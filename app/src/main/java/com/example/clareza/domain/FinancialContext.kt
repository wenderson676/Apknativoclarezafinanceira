package com.example.clareza.domain

import kotlinx.serialization.Serializable

enum class FinancialStage(val title: String, val description: String) {
    CRITICAL_DEBT(
        title = "Endividamento Crítico",
        description = "Prioridade absoluta: estancar juros abusivos e quitar dívidas essenciais/urgentes."
    ),
    MANAGEABLE_DEBT(
        title = "Dívida em Amortização",
        description = "Dívidas sob controle com plano estruturado de quitação (Método Avalanche)."
    ),
    UNSTABLE_NO_RESERVE(
        title = "Instabilidade / Sem Reserva",
        description = "Vulnerável a imprevistos. O foco imediato é construir uma reserva básica de segurança."
    ),
    BUILDING_RESERVE(
        title = "Construção de Reserva",
        description = "Finanças sob controle, acumulando o colchão de segurança de 3 a 6 meses de gastos fixos."
    ),
    STABILIZATION(
        title = "Estabilização Financeira",
        description = "Reserva consolidada e orçamento equilibrado. Pronto para metas de médio e longo prazo."
    ),
    CONSOLIDATION_INVESTMENT(
        title = "Consolidação & Expansão",
        description = "Excelente saúde financeira, alta taxa de poupança e foco em multiplicação e patrimônio."
    )
}

enum class FinancialRiskLevel(val label: String, val colorHex: String) {
    CRITICAL("Crítico", "#EF4444"),
    HIGH("Alto", "#F97316"),
    MODERATE("Moderado", "#F59E0B"),
    LOW("Baixo / Saudável", "#10B981")
}

@Serializable
data class CategoryExpense(
    val category: String,
    val amount: Double,
    val percentageOfExpenses: Double,
    val bucket: String
)

@Serializable
data class HealthScoreBreakdown(
    val cashFlowScore: Int, // 0 a 25
    val reserveScore: Int, // 0 a 25
    val debtScore: Int, // 0 a 25
    val budgetDisciplineScore: Int, // 0 a 25
    val totalScore: Int, // 0 a 100
    val explanationFactors: List<String> = emptyList()
)

@Serializable
data class FinancialContext(
    val userName: String?,
    val monthId: String,
    val budgetMode: String,

    // Renda e Saldos
    val totalIncome: Double,
    val totalExpenses: Double,
    val netSavingsTransfer: Double,
    val monthResult: Double,
    val liquidBalance: Double,
    val savingsBalance: Double,
    val totalBalance: Double,

    // Distribuição dos Potes (%)
    val needsPercentage: Double,
    val wantsPercentage: Double,
    val savingsPercentage: Double,
    val targetNeedsPercentage: Double,
    val targetWantsPercentage: Double,
    val targetSavingsPercentage: Double,

    // Dívidas
    val totalDebt: Double,
    val monthlyDebtMinimums: Double,
    val debtToIncomeRatio: Double,
    val highPriorityDebtCount: Int,
    val estimatedDebtPayoffMonths: Int,
    val isDebtCapacityRestricted: Boolean,

    // Reserva e Autonomia
    val averageEssentialMonthlyExpenses: Double,
    val monthsOfReserveCovered: Double, // Quanto tempo a reserva cobre de despesas essenciais reais

    // Diagnóstico e Classificação
    val healthScore: Int, // 0 a 100
    val healthScoreBreakdown: HealthScoreBreakdown,
    val stage: FinancialStage,
    val riskLevel: FinancialRiskLevel,
    val primaryStrategicFocus: String,

    // Categorias de Gastos Segregadas
    val topEssentialExpenses: List<CategoryExpense> = emptyList(),
    val topDiscretionaryExpenses: List<CategoryExpense> = emptyList(),
    val potentialMonthlySavings: Double = 0.0
) {
    /**
     * Fornece uma representação estruturada e concisa dos dados consolidados,
     * desacoplada de prompts ou formatações de IA.
     */
    fun toCompactContext(): String {
        return buildString {
            appendLine("=== CONTEXTO FINANCEIRO CLAREZA ===")
            if (!userName.isNullOrBlank()) appendLine("Usuário: $userName")
            appendLine("Mês: $monthId | Modelo: $budgetMode")
            appendLine("Fase: ${stage.title} | Risco: ${riskLevel.label} | Score: $healthScore/100")
            appendLine("Fluxo: Renda R$ ${"%.2f".format(totalIncome)} | Despesas R$ ${"%.2f".format(totalExpenses)} | Resultado R$ ${"%.2f".format(monthResult)}")
            appendLine("Saldos: Líquido R$ ${"%.2f".format(liquidBalance)} | Reserva R$ ${"%.2f".format(savingsBalance)} (${"%.1f".format(monthsOfReserveCovered)} meses de cobertura)")
            appendLine("Gastos Essenciais Médios: R$ ${"%.2f".format(averageEssentialMonthlyExpenses)}")
            appendLine("Potes: Necessidades ${"%.1f".format(needsPercentage)}% | Desejos ${"%.1f".format(wantsPercentage)}% | Poupança ${"%.1f".format(savingsPercentage)}%")
            if (totalDebt > 0) {
                appendLine("Dívidas: Total R$ ${"%.2f".format(totalDebt)} | Mínimos R$ ${"%.2f".format(monthlyDebtMinimums)}/mês | Comprometimento ${"%.1f".format(debtToIncomeRatio)}% | Urgentes: $highPriorityDebtCount")
            }
            if (topEssentialExpenses.isNotEmpty()) {
                appendLine("Top Essenciais: " + topEssentialExpenses.joinToString { "${it.category}: R$ ${"%.2f".format(it.amount)}" })
            }
            if (topDiscretionaryExpenses.isNotEmpty()) {
                appendLine("Top Desejos: " + topDiscretionaryExpenses.joinToString { "${it.category}: R$ ${"%.2f".format(it.amount)}" })
            }
            appendLine("Foco Estratégico: $primaryStrategicFocus")
        }
    }
}
