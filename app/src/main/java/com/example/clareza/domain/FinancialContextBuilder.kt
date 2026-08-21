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

        // Percentuais reais em relação à renda
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

        // Estimativa histórica de despesas essenciais (sem valores fixos fictícios)
        val averageEssentialExpenses = calculateHistoricalEssentialExpenses(
            allTransactions = allTransactions,
            currentMonthNeeds = needsSpent,
            currentIncome = totalIncome
        )

        // Meses de Reserva cobertos
        val monthsCovered = if (averageEssentialExpenses > 0) {
            savingsBalance / averageEssentialExpenses
        } else if (savingsBalance > 0) {
            12.0 // Se não há despesas e há reserva
        } else {
            0.0
        }

        // Categorias Segregadas: Essenciais (Necessidades) vs Discricionárias (Desejos)
        val nonPendingExpenses = nonPendingCurrent.filter { it.type == "expense" }

        val essentialCategories = nonPendingExpenses
            .filter { it.bucket == "Necessidades" }
            .groupBy { it.category }
            .map { (cat, txs) ->
                val amt = txs.sumOf { it.amount }
                CategoryExpense(
                    category = cat,
                    amount = amt,
                    percentageOfExpenses = if (needsSpent > 0) (amt / needsSpent) * 100 else 0.0,
                    bucket = "Necessidades"
                )
            }
            .sortedByDescending { it.amount }
            .take(5)

        val discretionaryCategories = nonPendingExpenses
            .filter { it.bucket == "Desejos" }
            .groupBy { it.category }
            .map { (cat, txs) ->
                val amt = txs.sumOf { it.amount }
                CategoryExpense(
                    category = cat,
                    amount = amt,
                    percentageOfExpenses = if (wantsSpent > 0) (amt / wantsSpent) * 100 else 0.0,
                    bucket = "Desejos"
                )
            }
            .sortedByDescending { it.amount }
            .take(5)

        // Health Score Explicável
        val healthScoreBreakdown = calculateHealthScoreBreakdown(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            monthsCovered = monthsCovered,
            savingsBalance = savingsBalance,
            totalDebt = totalDebt,
            highPriorityDebts = highPriorityDebts,
            debtToIncomeRatio = debtToIncomeRatio,
            wantsPct = wantsPct,
            targetWantsPct = targetWants * 100
        )

        // Determinação da Fase Financeira e Nível de Risco
        val (stage, riskLevel, focus) = determineStageAndRisk(
            totalIncome = totalIncome,
            totalDebt = totalDebt,
            highPriorityDebts = highPriorityDebts,
            debtToIncomeRatio = debtToIncomeRatio,
            savingsBalance = savingsBalance,
            monthsCovered = monthsCovered,
            liquidBalance = liquidBalance
        )

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
            averageEssentialMonthlyExpenses = averageEssentialExpenses,
            monthsOfReserveCovered = monthsCovered,
            healthScore = healthScoreBreakdown.totalScore,
            healthScoreBreakdown = healthScoreBreakdown,
            stage = stage,
            riskLevel = riskLevel,
            primaryStrategicFocus = focus,
            topEssentialExpenses = essentialCategories,
            topDiscretionaryExpenses = discretionaryCategories,
            potentialMonthlySavings = potentialSavings
        )
    }

    /**
     * Calcula média de despesas essenciais com base no histórico real dos últimos meses,
     * eliminando qualquer valor padrão arbitrário.
     */
    private fun calculateHistoricalEssentialExpenses(
        allTransactions: List<Transaction>,
        currentMonthNeeds: Double,
        currentIncome: Double
    ): Double {
        val nonPendingNeeds = allTransactions.filter { !it.isPending && it.type == "expense" && it.bucket == "Necessidades" }
        val byMonth = nonPendingNeeds.groupBy { it.monthId }

        if (byMonth.isNotEmpty()) {
            val monthlyAverages = byMonth.values.map { txList -> txList.sumOf { it.amount } }
            val avg = monthlyAverages.average()
            if (avg > 0) return avg
        }

        if (currentMonthNeeds > 0) return currentMonthNeeds
        if (currentIncome > 0) return currentIncome * 0.50 // Estimativa pelo orçamento 50%
        return 0.0
    }

    /**
     * Motor de Health Score Explicável baseado em 4 pilares de 25 pontos:
     * - Fluxo de Caixa (25 pts)
     * - Reserva e Segurança (25 pts)
     * - Endividamento e Alavancagem (25 pts)
     * - Disciplina e Desejos (25 pts)
     */
    private fun calculateHealthScoreBreakdown(
        totalIncome: Double,
        totalExpenses: Double,
        monthsCovered: Double,
        savingsBalance: Double,
        totalDebt: Double,
        highPriorityDebts: Int,
        debtToIncomeRatio: Double,
        wantsPct: Double,
        targetWantsPct: Double
    ): HealthScoreBreakdown {
        val factors = mutableListOf<String>()

        // 1. Fluxo de Caixa (25 pts)
        var cashFlow = 15
        if (totalIncome > 0) {
            val margin = totalIncome - totalExpenses
            if (margin > 0) {
                cashFlow = 25
                factors.add("Superávit mensal positivo (+25)")
            } else if (margin == 0.0) {
                cashFlow = 15
                factors.add("Orçamento no limite zero (+15)")
            } else {
                cashFlow = 5
                factors.add("Déficit mensal registrado (+5)")
            }
        } else {
            factors.add("Sem receitas registradas no mês (+15)")
        }

        // 2. Reserva de Emergência (25 pts)
        var reserve = 0
        when {
            monthsCovered >= 6.0 -> {
                reserve = 25
                factors.add("Reserva robusta >= 6 meses (+25)")
            }
            monthsCovered >= 3.0 -> {
                reserve = 20
                factors.add("Reserva saudável de 3 a 6 meses (+20)")
            }
            monthsCovered >= 1.0 -> {
                reserve = 12
                factors.add("Reserva básica em formação (+12)")
            }
            savingsBalance > 0 -> {
                reserve = 5
                factors.add("Reserva inicial abaixo de 1 mês (+5)")
            }
            else -> {
                reserve = 0
                factors.add("Sem reserva de emergência (0)")
            }
        }

        // 3. Dívidas (25 pts)
        var debt = 25
        when {
            totalDebt == 0.0 -> {
                debt = 25
                factors.add("Livre de dívidas (+25)")
            }
            highPriorityDebts > 0 -> {
                debt = 5
                factors.add("Dívidas de alta urgência presentes (+5)")
            }
            debtToIncomeRatio > 40.0 -> {
                debt = 8
                factors.add("Comprometimento elevado de renda com parcelas (+8)")
            }
            debtToIncomeRatio in 1.0..20.0 -> {
                debt = 20
                factors.add("Dívidas sob controle com baixo comprometimento (+20)")
            }
            else -> {
                debt = 14
                factors.add("Dívidas moderadas em amortização (+14)")
            }
        }

        // 4. Disciplina de Gastos (25 pts)
        var discipline = 18
        if (totalIncome > 0) {
            if (wantsPct <= targetWantsPct) {
                discipline = 25
                factors.add("Desejos dentro da meta do orçamento (+25)")
            } else if (wantsPct <= targetWantsPct * 1.25) {
                discipline = 15
                factors.add("Desejos ligeiramente acima da meta (+15)")
            } else {
                discipline = 5
                factors.add("Gastos discricionários excessivos (+5)")
            }
        }

        val total = (cashFlow + reserve + debt + discipline).coerceIn(0, 100)

        return HealthScoreBreakdown(
            cashFlowScore = cashFlow,
            reserveScore = reserve,
            debtScore = debt,
            budgetDisciplineScore = discipline,
            totalScore = total,
            explanationFactors = factors
        )
    }

    private fun determineStageAndRisk(
        totalIncome: Double,
        totalDebt: Double,
        highPriorityDebts: Int,
        debtToIncomeRatio: Double,
        savingsBalance: Double,
        monthsCovered: Double,
        liquidBalance: Double
    ): Triple<FinancialStage, FinancialRiskLevel, String> {
        // Distinção precisa: Endividamento Crítico vs Dívida em Amortização
        if (highPriorityDebts > 0 || (totalIncome > 0 && debtToIncomeRatio > 40.0)) {
            return Triple(
                FinancialStage.CRITICAL_DEBT,
                FinancialRiskLevel.CRITICAL,
                "Priorizar imediatamente o pagamento das dívidas essenciais/urgentes (Aluguel, Luz, Rotativo) antes de qualquer gasto não essencial."
            )
        }

        if (totalDebt > 0) {
            val isHighDebt = (totalIncome > 0 && debtToIncomeRatio > 25.0) || (savingsBalance < 500 && totalDebt > 2000)
            return Triple(
                FinancialStage.MANAGEABLE_DEBT,
                if (isHighDebt) FinancialRiskLevel.HIGH else FinancialRiskLevel.MODERATE,
                "Manter pagamentos mínimos em dia e direcionar a sobra financeira para acelerar a quitação pelo método Avalanche."
            )
        }

        // Sem dívidas: avaliar reserva e autonomia real
        if (savingsBalance <= 0 || monthsCovered < 0.5) {
            return Triple(
                FinancialStage.UNSTABLE_NO_RESERVE,
                if (liquidBalance < 200) FinancialRiskLevel.HIGH else FinancialRiskLevel.MODERATE,
                "Montar a Reserva de Emergência inicial para cobrir o primeiro mês de despesas essenciais e evitar novas dívidas diante de imprevistos."
            )
        }

        if (monthsCovered < 3.0) {
            return Triple(
                FinancialStage.BUILDING_RESERVE,
                FinancialRiskLevel.MODERATE,
                "Continuar aportando mensalmente no Cofrinho/Reserva até atingir de 3 a 6 meses de despesas essenciais guardadas."
            )
        }

        if (monthsCovered in 3.0..6.0) {
            return Triple(
                FinancialStage.STABILIZATION,
                FinancialRiskLevel.LOW,
                "Orçamento equilibrado e reserva consolidada. Fase ideal para definir metas financeiras e projetos de médio prazo."
            )
        }

        return Triple(
            FinancialStage.CONSOLIDATION_INVESTMENT,
            FinancialRiskLevel.LOW,
            "Saúde financeira sólida com ampla reserva. Foco em multiplicação de patrimônio, projetos de longo prazo e generosidade."
        )
    }
}
