package com.example.clareza.domain

import com.example.clareza.data.model.Debt
import com.example.clareza.util.FinanceUtils
import kotlin.math.ceil
import kotlin.math.max

data class DebtPlan(
    val totalDebt: Double,
    val monthlyCapacity: Double,
    val totalMonthlyMinimums: Double,
    val payoffMonths: Int,
    val highPriorityCount: Int,
    val mediumPriorityCount: Int,
    val lowPriorityCount: Int,
    val sortedDebts: List<Debt>,
    val recommendedMonthlyPayment: Double,
    val isCapacityRestricted: Boolean
)

object DebtEngine {

    /**
     * Calcula o plano estratégico de quitação de dívidas (Método Avalanche aprimorado)
     * Respeitando:
     * 1. A renda real e capacidade de pagamento real (sem assumir 300 reais do nada se a renda for 0)
     * 2. A soma das parcelas mensais mínimas cadastradas
     * 3. Priorização por urgência legal/essencial (Aluguel/Luz/Pensão/Agiota/Rotativo) e taxa de juros
     */
    fun calculateDebtPlan(debtsList: List<Debt>, totalIncome: Double): DebtPlan? {
        if (debtsList.isEmpty()) return null

        val totalDebt = debtsList.sumOf { it.totalAmount }
        val sumMonthlyPayments = debtsList.sumOf { it.monthlyPayment }

        // Capacidade real de amortização baseada na renda
        // Se a pessoa tiver renda, recomendamos até 30% da renda ou a soma das parcelas mínimas existentes
        val capacityFromIncome = if (totalIncome > 0) totalIncome * 0.30 else 0.0
        val effectiveMonthlyCapacity = max(capacityFromIncome, sumMonthlyPayments)

        val isRestricted = totalIncome <= 0 && sumMonthlyPayments <= 0

        // Cálculo de meses para quitação com base na capacidade efetiva
        val payoffMonths = when {
            totalDebt <= 0 -> 0
            effectiveMonthlyCapacity > 0 -> ceil(totalDebt / effectiveMonthlyCapacity).toInt()
            else -> 0 // Sem renda/capacidade definida
        }

        // Ordenação por prioridade: Máxima (1) > Média (2) > Baixa (3)
        // Desempate: Maior taxa de juros primeiro (Avalanche), seguido de maior montante
        val priorityOrder = mapOf("Máxima" to 1, "Média" to 2, "Baixa" to 3)
        val sortedDebts = debtsList.sortedWith(
            compareBy(
                { priorityOrder[FinanceUtils.DEBT_TYPES_INFO[it.type]?.priority ?: "Baixa"] ?: 3 },
                { -it.interestRate },
                { -it.totalAmount }
            )
        )

        val highCount = debtsList.count { FinanceUtils.DEBT_TYPES_INFO[it.type]?.priority == "Máxima" }
        val medCount = debtsList.count { FinanceUtils.DEBT_TYPES_INFO[it.type]?.priority == "Média" }
        val lowCount = debtsList.count { FinanceUtils.DEBT_TYPES_INFO[it.type]?.priority == "Baixa" }

        return DebtPlan(
            totalDebt = totalDebt,
            monthlyCapacity = effectiveMonthlyCapacity,
            totalMonthlyMinimums = sumMonthlyPayments,
            payoffMonths = payoffMonths,
            highPriorityCount = highCount,
            mediumPriorityCount = medCount,
            lowPriorityCount = lowCount,
            sortedDebts = sortedDebts,
            recommendedMonthlyPayment = max(effectiveMonthlyCapacity, 50.0),
            isCapacityRestricted = isRestricted
        )
    }
}
