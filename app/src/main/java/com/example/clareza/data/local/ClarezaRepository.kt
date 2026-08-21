package com.example.clareza.data.local

import com.example.clareza.data.model.Account
import com.example.clareza.data.model.AppState
import com.example.clareza.data.model.CustomCategories
import com.example.clareza.data.model.Debt
import com.example.clareza.data.model.Goal
import com.example.clareza.data.model.MonthlyData
import com.example.clareza.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ClarezaRepository(private val dao: ClarezaDao) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val accounts: Flow<List<Account>> = dao.getAllAccounts().map { list -> list.map { it.toModel() } }
    val transactions: Flow<List<Transaction>> = dao.getAllTransactions().map { list -> list.map { it.toModel() } }
    val goals: Flow<List<Goal>> = dao.getAllGoals().map { list -> list.map { it.toModel() } }
    val debts: Flow<List<Debt>> = dao.getAllDebts().map { list -> list.map { it.toModel() } }
    val monthNotes: Flow<List<MonthNoteEntity>> = dao.getAllMonthNotes()
    val settings: Flow<Map<String, String>> = dao.getAllSettings().map { list -> list.associate { it.key to it.value } }

    fun getTransactionsForMonth(monthId: String): Flow<List<Transaction>> {
        return dao.getTransactionsForMonth(monthId).map { list -> list.map { it.toModel() } }
    }

    suspend fun saveAccount(account: Account) {
        dao.insertAccount(AccountEntity.fromModel(account))
    }

    suspend fun deleteAccount(id: String) {
        dao.deleteAccount(id)
    }

    suspend fun saveTransaction(transaction: Transaction) {
        dao.insertTransaction(TransactionEntity.fromModel(transaction))
    }

    suspend fun saveTransactions(transactions: List<Transaction>) {
        dao.insertTransactions(transactions.map { TransactionEntity.fromModel(it) })
    }

    suspend fun deleteTransaction(id: String) {
        dao.deleteTransaction(id)
    }

    suspend fun toggleTransactionPending(id: String, isPending: Boolean) {
        dao.updateTransactionPending(id, isPending)
    }

    suspend fun saveGoal(goal: Goal) {
        dao.insertGoal(GoalEntity.fromModel(goal))
    }

    suspend fun deleteGoal(id: String) {
        dao.deleteGoal(id)
    }

    suspend fun saveDebt(debt: Debt) {
        dao.insertDebt(DebtEntity.fromModel(debt))
    }

    suspend fun deleteDebt(id: String) {
        dao.deleteDebt(id)
    }

    suspend fun saveMonthNote(monthId: String, note: String) {
        dao.insertMonthNote(MonthNoteEntity(monthId, note))
    }

    suspend fun setSetting(key: String, value: String) {
        dao.setSetting(UserSettingEntity(key, value))
    }

    suspend fun getSetting(key: String): String? {
        return dao.getSetting(key)
    }

    suspend fun resetAllData() {
        dao.clearAllData()
        // Re-insert default accounts
        dao.insertAccounts(
            listOf(
                AccountEntity("banco", "Banco", "🏦", "banco", 0.0, true),
                AccountEntity("reserva", "Reserva (Cofrinho)", "💰", "reserva", 0.0, false),
                AccountEntity("carteira", "Carteira (Dinheiro Físico)", "💵", "carteira", 0.0, false)
            )
        )
    }

    suspend fun exportFullState(
        accountsList: List<Account>,
        transactionsList: List<Transaction>,
        goalsList: List<Goal>,
        debtsList: List<Debt>,
        monthNotesList: List<MonthNoteEntity>,
        userName: String?,
        budgetMode: String,
        customCategories: CustomCategories
    ): String {
        val monthlyMap = mutableMapOf<String, MonthlyData>()
        
        // Group transactions by monthId
        transactionsList.groupBy { it.monthId }.forEach { (mId, txs) ->
            val note = monthNotesList.find { it.monthId == mId }?.note ?: ""
            monthlyMap[mId] = MonthlyData(transactions = txs, devotionalNote = note)
        }

        // Also add months that only have notes
        monthNotesList.forEach { noteEntity ->
            if (!monthlyMap.containsKey(noteEntity.monthId)) {
                monthlyMap[noteEntity.monthId] = MonthlyData(transactions = emptyList(), devotionalNote = noteEntity.note)
            }
        }

        val appState = AppState(
            userName = userName,
            budgetMode = budgetMode,
            accounts = accountsList,
            goals = goalsList,
            debts = debtsList,
            monthlyData = monthlyMap,
            customCategories = customCategories
        )

        return json.encodeToString(AppState.serializer(), appState)
    }

    suspend fun importFullState(jsonString: String) {
        val appState = json.decodeFromString(AppState.serializer(), jsonString)

        dao.clearAllData()

        if (appState.accounts.isNotEmpty()) {
            dao.insertAccounts(appState.accounts.map { AccountEntity.fromModel(it) })
        } else {
            dao.insertAccounts(
                listOf(
                    AccountEntity("banco", "Banco", "🏦", "banco", 0.0, true),
                    AccountEntity("reserva", "Reserva (Cofrinho)", "💰", "reserva", 0.0, false),
                    AccountEntity("carteira", "Carteira (Dinheiro Físico)", "💵", "carteira", 0.0, false)
                )
            )
        }

        if (appState.goals.isNotEmpty()) {
            dao.insertGoals(appState.goals.map { GoalEntity.fromModel(it) })
        }

        if (appState.debts.isNotEmpty()) {
            dao.insertDebts(appState.debts.map { DebtEntity.fromModel(it) })
        }

        val allTxs = mutableListOf<TransactionEntity>()
        val allNotes = mutableListOf<MonthNoteEntity>()

        appState.monthlyData.forEach { (mId, mData) ->
            mData.transactions.forEach { tx ->
                allTxs.add(TransactionEntity.fromModel(tx.copy(monthId = mId)))
            }
            if (mData.devotionalNote.isNotBlank()) {
                allNotes.add(MonthNoteEntity(mId, mData.devotionalNote))
            }
        }

        if (allTxs.isNotEmpty()) {
            dao.insertTransactions(allTxs)
        }

        if (allNotes.isNotEmpty()) {
            dao.insertMonthNotes(allNotes)
        }

        if (appState.userName != null) {
            dao.setSetting(UserSettingEntity("userName", appState.userName))
        }
        dao.setSetting(UserSettingEntity("budgetMode", appState.budgetMode))

        val customCatsJson = json.encodeToString(CustomCategories.serializer(), appState.customCategories)
        dao.setSetting(UserSettingEntity("customCategories", customCatsJson))
    }
}
