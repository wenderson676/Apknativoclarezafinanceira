package com.example.clareza.domain

import com.example.clareza.data.model.Account
import com.example.clareza.data.model.Debt
import com.example.clareza.data.model.Goal
import com.example.clareza.data.model.Transaction
import com.example.clareza.util.FinanceUtils
import kotlin.math.max

object FinancialContextBuilder {

    fun build(
        accountsList: List<Account>,
        allTransactions: List<Transaction>,
        currentMonthTransactions: List<Transaction>,
        goalsList: List<Goal>,
        debtsList: List<Debt>,
        userName: String?,
        monthId: String,
        budgetMode: String
    ): FinancialContext {
        // Balanços das contas
        val (_, totalBalance, liquidSavingsPair) = FinanceEngine.calculateAccountBalances(
            accountsList = accountsList,
            allTransactions = allTransactions
        )
        val (liquidBalance, savingsBalance) = liquidSavingsPair

        // Fluxo do mês corrente
        val isReserva = { id: String? ->
            id == "reserva" || accountsList.find { it.id == id }?.type == "reserva"
        }

        val nonPendingCurrent = currentMonthTransactions.filter { !it.isPending }
        val totalIncome = nonPendingCurrent
            .filter { it.type == "income" && !isReserva(it.account) }
            .sumOf { it.amount }

        val needsSpent = nonPendingCurrent
            .filter { it.type == "expense" && !isReserva(it.account) && it.bucket == "Necessidades" }
            .sumOf { it.amount }

        val wantsSpent = nonPendingCurrent
            .filter { it.type == "expense" && !isReserva(it.account) && it.bucket == "Desejos" }
            .sumOf { it.amount }

        val savingsSpent = nonPendingCurrent
            .filter { it.type == "expense" && it.bucket == "Reserva/Dívidas" }
            .sumOf { it.amount }

        val totalExpenses = needsSpent + wantsSpent

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

        // Percentuais reais
        val refIncome = if (totalIncome > 0) totalIncome else 0.0
        val needsPct = if (refIncome > 0) (needsSpent / refIncome) * 100 else 0.0
        val wantsPct = if (refIncome > 0) (wantsSpent / refIncome) * 100 else 0.0
        val savingsPct = if (refIncome > 0) ((savingsSpent + max(0.0, netSavingsTransfer)) / refIncome) * 100 else 0.0

        val modeInfo = FinanceUtils.BUDGET_MODES_INFO[budgetMode] ?: FinanceUtils.BUDGET_MODES_INFO["50-30-20"]!!
        val targetNeeds = modeInfo.ratios["Necessidades"] ?: 0.50
        val targetWants = modeInfo.ratios["Desejos"] ?: 0.30
        val targetSavings = modeInfo.ratios["Reserva/Dívidas"] ?: 0.20

        // Dívidas
        val debtPlan = DebtEngine.calculateDebtPlan(debtsList, totalIncome)
        val totalDebt = debtPlan?.totalDebt ?: 0.0
        val monthlyDebtMinimums = debtPlan?.totalMonthlyMinimums ?: 0.0
        val highPriorityDebts = debtPlan?.highPriorityCount ?: 0
        val debtPayoffMonths = debtPlan?.payoffMonths ?: 0
        val debtRestricted = debtPlan?.isCapacityRestricted ?: false
        val debtToIncomeRatio = if (totalIncome > 0) (monthlyDebtMinimums / totalIncome) * 100 else 0.0

        // Reserva e Autonomia (Quantos meses de gastos básicos a reserva atual cobre)
        val basicExpenses = if (needsSpent > 0) needsSpent else 1500.0
        val monthsCovered = if (basicExpenses > 0) savingsBalance / basicExpenses else 0.0

        // Maiores categorias de gastos
        val expenseCategoryMap = nonPendingCurrent
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val totalAllExp = expenseCategoryMap.values.sum()
        val topCategories = expenseCategoryMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .map {
                CategoryExpense(
                    category = it.key,
                    amount = it.value,
                    percentageOfExpenses = if (totalAllExp > 0) (it.value / totalAllExp) * 100 else 0.0
                )
            }

        // Determinação da Fase Financeira e Nível de Risco
        val (stage, riskLevel, focus) = determineStageAndRisk(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            totalDebt = totalDebt,
            highPriorityDebts = highPriorityDebts,
            debtToIncomeRatio = debtToIncomeRatio,
            savingsBalance = savingsBalance,
            monthsCovered = monthsCovered,
            wantsPct = wantsPct,
            targetWants = targetWants
        )

        // Cálculo de Health Score (0 - 100)
        var score = 70
        if (totalIncome > 0 && totalExpenses <= totalIncome) score += 10 else score -= 15
        if (debtToIncomeRatio > 30) score -= 20 else if (totalDebt == 0.0) score += 10
        if (highPriorityDebts > 0) score -= 15
        if (monthsCovered >= 3.0) score += 15 else if (monthsCovered < 0.5) score -= 10
        if (wantsPct <= targetWants * 100) score += 5 else score -= 5
        val healthScore = score.coerceIn(0, 100)

        // Potencial de economia (cortes em desejos excedentes)
        val maxWantsAllowed = refIncome * targetWants
        val potentialSavings = max(0.0, wantsSpent - maxWantsAllowed)

        return FinancialContext(
            userName = userName,
            monthId = monthId,
            budgetMode = budgetMode,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netSavingsTransfer = netSavingsTransfer,
            monthResult = monthResult,
            liquidBalance = liquidBalance,
            savingsBalance = savingsBalance,
            totalBalance = totalBalance,
            needsPercentage = needsPct,
            wantsPercentage = wantsPct,
            savingsPercentage = savingsPct,
            targetNeedsPercentage = targetNeeds,
            targetWantsPercentage = targetWants,
            targetSavingsPercentage = targetSavings,
            totalDebt = totalDebt,
            monthlyDebtMinimums = monthlyDebtMinimums,
            debtToIncomeRatio = debtToIncomeRatio,
            highPriorityDebtCount = highPriorityDebts,
            estimatedDebtPayoffMonths = debtPayoffMonths,
            isDebtCapacityRestricted = debtRestricted,
            monthsOfReserveCovered = monthsCovered,
            healthScore = healthScore,
            stage = stage,
            riskLevel = riskLevel,
            primaryStrategicFocus = focus,
            topExpenseCategories = topCategories,
            potentialMonthlySavings = potentialSavings
        )
    }

    private fun determineStageAndRisk(
        totalIncome: Double,
        totalExpenses: Double,
        totalDebt: Double,
        highPriorityDebts: Int,
        debtToIncomeRatio: Double,
        savingsBalance: Double,
        monthsCovered: Double,
        wantsPct: Double,
        targetWants: Double
    ): Triple<FinancialStage, FinancialRiskLevel, String> {
        return when {
            highPriorityDebts > 0 || debtToIncomeRatio > 40.0 -> {
                Triple(
                    FinancialStage.CRITICAL_DEBT,
                    FinancialRiskLevel.CRITICAL,
                    "Priorizar imediatamente o pagamento das dívidas com juros altos ou risco de corte (Aluguel/Luz/Rotativo) antes de qualquer gasto não essencial."
                )
            }
            totalDebt > 0 -> {
                Triple(
                    FinancialStage.CRITICAL_DEBT,
                    FinancialRiskLevel.HIGH,
                    "Direcionar a capacidade de amortização para quitar dívidas pelo método Avalanche enquanto mantém pagamentos mínimos em dia."
                )
            }
            savingsBalance < 1000.0 || monthsCovered < 1.0 -> {
                Triple(
                    FinancialStage.UNSTABLE_NO_RESERVE,
                    FinancialRiskLevel.HIGH,
                    "Montar a Reserva de Emergência inicial de pelo menos R$ 1.000 para evitar cair em novas dívidas diante de imprevistos."
                )
            }
            monthsCovered < 3.0 -> {
                Triple(
                    FinancialStage.BUILDING_RESERVE,
                    FinancialRiskLevel.MODERATE,
                    "Continuar alocando economias mensais até completar 3 a 6 meses de despesas essenciais guardadas no Cofrinho/Reserva."
                )
            }
            monthsCovered in 3.0..6.0 -> {
                Triple(
                    FinancialStage.STABILIZATION,
                    FinancialRiskLevel.LOW,
                    "Orçamento estável e reserva consolidada. Excelente momento para definir metas de médio prazo e sonhos específicos."
                )
            }
            else -> {
                Triple(
                    FinancialStage.CONSOLIDATION_INVESTMENT,
                    FinancialRiskLevel.LOW,
                    "Saúde financeira sólida com reserva robusta. Foco em multiplicação de patrimônio, projetos de longo prazo e generosidade."
                )
            }
        }
    }
}
