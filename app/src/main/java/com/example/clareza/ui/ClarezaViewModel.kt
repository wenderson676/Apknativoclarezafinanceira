package com.example.clareza.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clareza.data.local.AppDatabase
import com.example.clareza.data.local.ClarezaRepository
import com.example.clareza.data.model.Account
import com.example.clareza.data.model.CustomCategories
import com.example.clareza.data.model.Debt
import com.example.clareza.data.model.Goal
import com.example.clareza.data.model.Transaction
import com.example.clareza.util.DiagnosticResult
import com.example.clareza.util.FinanceUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

@Suppress("UNCHECKED_CAST")
fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    f7: Flow<T7>,
    f8: Flow<T8>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R
): Flow<R> = kotlinx.coroutines.flow.combine(listOf(f1, f2, f3, f4, f5, f6, f7, f8)) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5,
        args[5] as T6,
        args[6] as T7,
        args[7] as T8
    )
}

data class BucketProgress(
    val bucketName: String,
    val spent: Double,
    val limit: Double,
    val ratio: Double,
    val percentage: Double,
    val status: String // "normal", "warning", "danger"
)

data class DebtPlan(
    val totalDebt: Double,
    val monthlyCapacity: Double,
    val payoffMonths: Int,
    val highPriorityCount: Int,
    val mediumPriorityCount: Int,
    val lowPriorityCount: Int,
    val sortedDebts: List<Debt>
)

data class ClarezaUiState(
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val userName: String? = null,
    val budgetMode: String = "50-30-20",
    val isDarkMode: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val accountBalances: Map<String, Double> = emptyMap(),
    val totalBalance: Double = 0.0,
    val liquidBalance: Double = 0.0,
    val savingsBalance: Double = 0.0,
    val currentMonthTransactions: List<Transaction> = emptyList(),
    val allTransactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netSavingsTransfer: Double = 0.0,
    val monthResult: Double = 0.0,
    val bucketProgress: List<BucketProgress> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val debtPlan: DebtPlan? = null,
    val monthNote: String = "",
    val customCategories: CustomCategories = CustomCategories(),
    val diagnosticResult: DiagnosticResult? = null,
    val dailyVerse: String = FinanceUtils.getRandomVerse()
)

class ClarezaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClarezaRepository
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    private val _selectedYearMonth = MutableStateFlow(YearMonth.now())
    private val _isDarkMode = MutableStateFlow(false)
    private val _verse = MutableStateFlow(FinanceUtils.getRandomVerse())

    init {
        val db = AppDatabase.getInstance(application)
        repository = ClarezaRepository(db.clarezaDao())
    }

    val uiState: StateFlow<ClarezaUiState> = combine(
        _selectedYearMonth,
        repository.accounts,
        repository.transactions,
        repository.goals,
        repository.debts,
        repository.monthNotes,
        repository.settings,
        _isDarkMode
    ) { yearMonth, accountsList, allTxList, goalsList, debtsList, notesList, settingsMap, isDark ->

        val monthId = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val currentMonthTxs = allTxList.filter { it.monthId == monthId }
        val note = notesList.find { it.monthId == monthId }?.note ?: ""
        val userName = settingsMap["userName"]
        val budgetMode = settingsMap["budgetMode"] ?: "50-30-20"
        val customCats = try {
            settingsMap["customCategories"]?.let { json.decodeFromString(CustomCategories.serializer(), it) }
                ?: CustomCategories()
        } catch (e: Exception) {
            CustomCategories()
        }

        // Account balances calculation
        val balances = mutableMapOf<String, Double>()
        accountsList.forEach { acc ->
            balances[acc.id] = acc.initialBalance
        }
        if (!balances.containsKey("banco")) balances["banco"] = 0.0
        if (!balances.containsKey("reserva")) balances["reserva"] = 0.0
        if (!balances.containsKey("carteira")) balances["carteira"] = 0.0

        allTxList.filter { !it.isPending }.forEach { tx ->
            val act = tx.account.ifBlank { "banco" }
            val toAct = tx.toAccount

            when (tx.type) {
                "income" -> {
                    balances[act] = (balances[act] ?: 0.0) + tx.amount
                }
                "expense" -> {
                    balances[act] = (balances[act] ?: 0.0) - tx.amount
                }
                "transfer_to_savings" -> {
                    balances[act] = (balances[act] ?: 0.0) - tx.amount
                    balances["reserva"] = (balances["reserva"] ?: 0.0) + tx.amount
                }
                "transfer_from_savings" -> {
                    balances["reserva"] = (balances["reserva"] ?: 0.0) - tx.amount
                    balances[act] = (balances[act] ?: 0.0) + tx.amount
                }
                "transfer_between_accounts" -> {
                    balances[act] = (balances[act] ?: 0.0) - tx.amount
                    if (!toAct.isNullOrBlank()) {
                        balances[toAct] = (balances[toAct] ?: 0.0) + tx.amount
                    }
                }
            }
        }

        val isReserva = { id: String? ->
            id == "reserva" || accountsList.find { it.id == id }?.type == "reserva"
        }

        val totalBalance = balances.values.sum()
        val savingsBalance = balances.filter { isReserva(it.key) }.values.sum()
        val liquidBalance = totalBalance - savingsBalance

        // Current month totals
        val nonPendingCurrent = currentMonthTxs.filter { !it.isPending }
        val totalIncome = nonPendingCurrent
            .filter { it.type == "income" && !isReserva(it.account) }
            .sumOf { it.amount }

        val totalExpenses = nonPendingCurrent
            .filter { it.type == "expense" && !isReserva(it.account) && it.bucket != "Reserva/Dívidas" }
            .sumOf { it.amount }

        val netSavingsTransfer = nonPendingCurrent.sumOf { tx ->
            val act = tx.account.ifBlank { "banco" }
            val toAct = tx.toAccount

            if (tx.type == "expense" && tx.bucket == "Reserva/Dívidas") {
                if (isReserva(act)) -tx.amount else tx.amount
            } else if (tx.type == "transfer_to_savings" || (tx.type == "income" && isReserva(act)) || (tx.type == "transfer_between_accounts" && isReserva(toAct))) {
                tx.amount
            } else if (tx.type == "transfer_from_savings" || (tx.type == "expense" && isReserva(act)) || (tx.type == "transfer_between_accounts" && isReserva(act))) {
                -tx.amount
            } else {
                0.0
            }
        }

        val monthResult = totalIncome - totalExpenses - netSavingsTransfer

        // Bucket progress
        val modeInfo = FinanceUtils.BUDGET_MODES_INFO[budgetMode] ?: FinanceUtils.BUDGET_MODES_INFO["50-30-20"]!!
        val baseForBudget = if (totalIncome > 0) totalIncome else maxOf(1.0, liquidBalance)

        val bucketList = listOf("Necessidades", "Desejos", "Reserva/Dívidas")
        val bucketProgress = bucketList.map { bucketName ->
            val ratio = modeInfo.ratios[bucketName] ?: 0.0
            val limit = baseForBudget * ratio

            val spent = if (bucketName == "Reserva/Dívidas") {
                maxOf(0.0, netSavingsTransfer)
            } else {
                nonPendingCurrent
                    .filter { it.type == "expense" && it.bucket == bucketName && !isReserva(it.account) }
                    .sumOf { it.amount }
            }

            val percentage = if (limit > 0) (spent / limit) * 100.0 else 0.0
            val status = when {
                percentage > 100.0 -> "danger"
                percentage > 85.0 -> "warning"
                else -> "normal"
            }

            BucketProgress(
                bucketName = bucketName,
                spent = spent,
                limit = limit,
                ratio = ratio,
                percentage = percentage,
                status = status
            )
        }

        // Debt plan calculations (Avalanche priority)
        val debtPlan = if (debtsList.isNotEmpty()) {
            val totalDebt = debtsList.sumOf { it.totalAmount }
            val monthlyCapacity = maxOf(300.0, totalIncome * 0.30)
            val payoffMonths = if (monthlyCapacity > 0) kotlin.math.ceil(totalDebt / monthlyCapacity).toInt() else 0

            val priorityOrder = mapOf("Máxima" to 1, "Média" to 2, "Baixa" to 3)
            val sorted = debtsList.sortedWith(
                compareBy(
                    { priorityOrder[FinanceUtils.DEBT_TYPES_INFO[it.type]?.priority ?: "Baixa"] ?: 3 },
                    { -it.interestRate },
                    { -it.totalAmount }
                )
            )

            val highCount = debtsList.count { FinanceUtils.DEBT_TYPES_INFO[it.type]?.priority == "Máxima" }
            val medCount = debtsList.count { FinanceUtils.DEBT_TYPES_INFO[it.type]?.priority == "Média" }
            val lowCount = debtsList.count { FinanceUtils.DEBT_TYPES_INFO[it.type]?.priority == "Baixa" }

            DebtPlan(
                totalDebt = totalDebt,
                monthlyCapacity = monthlyCapacity,
                payoffMonths = payoffMonths,
                highPriorityCount = highCount,
                mediumPriorityCount = medCount,
                lowPriorityCount = lowCount,
                sortedDebts = sorted
            )
        } else null

        // Diagnostics
        val diagnostics = FinanceUtils.calculateDiagnostic(
            transactions = currentMonthTxs,
            currentBalance = liquidBalance,
            budgetMode = budgetMode,
            accounts = accountsList,
            debts = debtsList
        )

        ClarezaUiState(
            selectedYearMonth = yearMonth,
            userName = userName,
            budgetMode = budgetMode,
            isDarkMode = isDark,
            accounts = accountsList,
            accountBalances = balances,
            totalBalance = totalBalance,
            liquidBalance = liquidBalance,
            savingsBalance = savingsBalance,
            currentMonthTransactions = currentMonthTxs,
            allTransactions = allTxList,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netSavingsTransfer = netSavingsTransfer,
            monthResult = monthResult,
            bucketProgress = bucketProgress,
            goals = goalsList,
            debts = debtsList,
            debtPlan = debtPlan,
            monthNote = note,
            customCategories = customCats,
            diagnosticResult = diagnostics,
            dailyVerse = _verse.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClarezaUiState()
    )

    fun changeMonth(yearMonth: YearMonth) {
        _selectedYearMonth.value = yearMonth
    }

    fun previousMonth() {
        _selectedYearMonth.value = _selectedYearMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedYearMonth.value = _selectedYearMonth.value.plusMonths(1)
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun refreshVerse() {
        _verse.value = FinanceUtils.getRandomVerse()
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            repository.setSetting("userName", name)
        }
    }

    fun setBudgetMode(mode: String) {
        viewModelScope.launch {
            repository.setSetting("budgetMode", mode)
        }
    }

    fun addTransaction(
        amount: Double,
        description: String,
        bucket: String,
        category: String,
        date: String,
        isPending: Boolean,
        account: String,
        toAccount: String?,
        type: String,
        repeatCount: Int = 1,
        frequency: String = "none" // "none", "weekly", "biweekly", "monthly"
    ) {
        viewModelScope.launch {
            val baseDate = try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }
            val txs = mutableListOf<Transaction>()

            for (i in 0 until repeatCount) {
                val txDate = when (frequency) {
                    "weekly" -> baseDate.plusWeeks(i.toLong())
                    "biweekly" -> baseDate.plusWeeks((i * 2).toLong())
                    "monthly" -> baseDate.plusMonths(i.toLong())
                    else -> baseDate
                }

                val monthId = txDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val dateStr = txDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val desc = if (repeatCount > 1) "$description (${i + 1}/$repeatCount)" else description

                txs.add(
                    Transaction(
                        id = UUID.randomUUID().toString(),
                        monthId = monthId,
                        amount = amount,
                        description = desc,
                        bucket = bucket,
                        category = category,
                        date = dateStr,
                        isPending = isPending,
                        account = account,
                        toAccount = toAccount,
                        type = type
                    )
                )
            }

            repository.saveTransactions(txs)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val monthId = try {
                LocalDate.parse(transaction.date).format(DateTimeFormatter.ofPattern("yyyy-MM"))
            } catch (e: Exception) {
                transaction.monthId
            }
            repository.saveTransaction(transaction.copy(monthId = monthId))
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun toggleTransactionPending(id: String, currentPending: Boolean) {
        viewModelScope.launch {
            repository.toggleTransactionPending(id, !currentPending)
        }
    }

    fun saveGoal(goal: Goal) {
        viewModelScope.launch {
            val id = if (goal.id.isBlank()) UUID.randomUUID().toString() else goal.id
            repository.saveGoal(goal.copy(id = id))
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    fun depositToGoal(goal: Goal, amount: Double) {
        viewModelScope.launch {
            val newAmount = goal.currentAmount + amount
            repository.saveGoal(goal.copy(currentAmount = newAmount))
            // Also record a transfer/reserve transaction
            val today = LocalDate.now()
            val monthId = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            repository.saveTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    monthId = monthId,
                    amount = amount,
                    description = "Aporte: ${goal.title}",
                    bucket = "Reserva/Dívidas",
                    category = "Meta / Sonho Específico",
                    date = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    isPending = false,
                    account = "banco",
                    toAccount = "reserva",
                    type = "transfer_to_savings"
                )
            )
        }
    }

    fun withdrawFromGoal(goal: Goal, amount: Double) {
        viewModelScope.launch {
            val newAmount = maxOf(0.0, goal.currentAmount - amount)
            repository.saveGoal(goal.copy(currentAmount = newAmount))
            val today = LocalDate.now()
            val monthId = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            repository.saveTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    monthId = monthId,
                    amount = amount,
                    description = "Resgate: ${goal.title}",
                    bucket = "Renda",
                    category = "Resgate de Reserva",
                    date = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    isPending = false,
                    account = "reserva",
                    toAccount = "banco",
                    type = "transfer_from_savings"
                )
            )
        }
    }

    fun saveDebt(debt: Debt) {
        viewModelScope.launch {
            val id = if (debt.id.isBlank()) UUID.randomUUID().toString() else debt.id
            repository.saveDebt(debt.copy(id = id))
        }
    }

    fun deleteDebt(id: String) {
        viewModelScope.launch {
            repository.deleteDebt(id)
        }
    }

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            val id = if (account.id.isBlank()) UUID.randomUUID().toString() else account.id
            repository.saveAccount(account.copy(id = id))
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            repository.deleteAccount(id)
        }
    }

    fun saveMonthNote(monthId: String, note: String) {
        viewModelScope.launch {
            repository.saveMonthNote(monthId, note)
        }
    }

    fun addCustomCategory(bucket: String, newCatName: String) {
        viewModelScope.launch {
            val current = uiState.value.customCategories
            val updated = when (bucket) {
                "Renda" -> current.copy(income = current.income + newCatName)
                "Transferência" -> current.copy(transfer = current.transfer + newCatName)
                else -> current.copy(expense = current.expense + newCatName)
            }
            val serialized = json.encodeToString(CustomCategories.serializer(), updated)
            repository.setSetting("customCategories", serialized)
        }
    }

    suspend fun exportData(): String {
        val state = uiState.value
        return repository.exportFullState(
            accountsList = state.accounts,
            transactionsList = state.allTransactions,
            goalsList = state.goals,
            debtsList = state.debts,
            monthNotesList = emptyList(),
            userName = state.userName,
            budgetMode = state.budgetMode,
            customCategories = state.customCategories
        )
    }

    suspend fun importData(jsonString: String) {
        repository.importFullState(jsonString)
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }
}
