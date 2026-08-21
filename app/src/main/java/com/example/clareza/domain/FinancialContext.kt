package com.example.clareza.domain

import kotlinx.serialization.Serializable

enum class FinancialStage(val title: String, val description: String) {
    CRITICAL_DEBT(
        title = "Endividamento Crítico",
        description = "Prioridade absoluta: estancar juros abusivos e quitar dívidas essenciais/urgentes."
    ),
    UNSTABLE_NO_RESERVE(
        title = "Instabilidade / Sem Reserva",
        description = "Vulnerável a imprevistos. O foco imediato é construir uma reserva básica inicial."
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
    val percentageOfExpenses: Double
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
    val monthsOfReserveCovered: Double, // Quanto tempo a reserva cobre de despesas fixas

    // Diagnóstico e Classificação
    val healthScore: Int, // 0 a 100
    val stage: FinancialStage,
    val riskLevel: FinancialRiskLevel,
    val primaryStrategicFocus: String,

    // Maiores Ofensores de Gastos
    val topExpenseCategories: List<CategoryExpense> = emptyList(),
    val potentialMonthlySavings: Double = 0.0
) {
    /**
     * Gera um resumo compacto e de altíssima densidade informacional,
     * pronto para alimentar modelos de IA locais/offline ou motores analíticos.
     */
    fun toCompactPrompt(): String {
        return buildString {
            appendLine("=== CONTEXTO FINANCEIRO CLAREZA ===")
            if (!userName.isNullOrBlank()) appendLine("Usuário: $userName")
            appendLine("Mês: $monthId | Modelo: $budgetMode")
            appendLine("Fase Atual: ${stage.title} | Risco: ${riskLevel.label} | Score: $healthScore/100")
            appendLine("--- FLUXO ---")
            appendLine("Renda: R$ ${"%.2f".format(totalIncome)} | Despesas: R$ ${"%.2f".format(totalExpenses)} | Resultado: R$ ${"%.2f".format(monthResult)}")
            appendLine("Saldo Líquido: R$ ${"%.2f".format(liquidBalance)} | Reserva Acumulada: R$ ${"%.2f".format(savingsBalance)} (Cobre ${"%.1f".format(monthsOfReserveCovered)} meses)")
            appendLine("--- POTES ---")
            appendLine("Necessidades: ${"%.1f".format(needsPercentage)}% (Alvo: ${"%.0f".format(targetNeedsPercentage * 100)}%)")
            appendLine("Desejos: ${"%.1f".format(wantsPercentage)}% (Alvo: ${"%.0f".format(targetWantsPercentage * 100)}%)")
            appendLine("Poupança/Dívidas: ${"%.1f".format(savingsPercentage)}% (Alvo: ${"%.0f".format(targetSavingsPercentage * 100)}%)")
            if (totalDebt > 0) {
                appendLine("--- DÍVIDAS ---")
                appendLine("Dívida Total: R$ ${"%.2f".format(totalDebt)} (Comprometimento de Renda: ${"%.1f".format(debtToIncomeRatio)}%)")
                appendLine("Parcelas Mínimas: R$ ${"%.2f".format(monthlyDebtMinimums)}/mês | Dívidas Urgentes: $highPriorityDebtCount")
                if (estimatedDebtPayoffMonths > 0) {
                    appendLine("Previsão Quitação: $estimatedDebtPayoffMonths meses")
                }
            }
            if (topExpenseCategories.isNotEmpty()) {
                appendLine("--- PRINCIPAIS GASTOS ---")
                topExpenseCategories.take(3).forEach {
                    appendLine("- ${it.category}: R$ ${"%.2f".format(it.amount)} (${"%.1f".format(it.percentageOfExpenses)}%)")
                }
            }
            appendLine("--- FOCO ESTRATÉGICO ---")
            appendLine(primaryStrategicFocus)
        }
    }
}
