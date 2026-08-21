package com.example.clareza.domain

import com.example.clareza.data.model.Debt
import com.example.clareza.util.FinanceUtils
import kotlin.math.ceil
import kotlin.math.max

data class DebtPlan(
    val totalDebt: Double,
    val monthlyCapacity: Double,
    val totalMonthlyMinimums: Double,
    val extraPaymentCapacity: Double,
    val recommendedMonthlyPayment: Double,
    val payoffMonths: Int,
    val highPriorityCount: Int,
    val mediumPriorityCount: Int,
    val lowPriorityCount: Int,
    val sortedDebts: List<Debt>,
    val isCapacityRestricted: Boolean
)

object DebtEngine {

    /**
     * Calcula o plano estratégico de quitação de dívidas (Método Avalanche aprimorado)
     * Respeitando:
     * 1. A renda real e capacidade de pagamento real (sem inventar capacidade se a renda for 0)
     * 2. A soma das parcelas mensais mínimas cadastradas
     * 3. Priorização por urgência legal/essencial (Aluguel/Luz/Pensão/Agiota/Rotativo) e taxa de juros
     */
    fun calculateDebtPlan(debtsList: List<Debt>, totalIncome: Double): DebtPlan? {
        if (debtsList.isEmpty()) return null

        val totalDebt = debtsList.sumOf { it.totalAmount }
        val sumMonthlyPayments = debtsList.sumOf { it.monthlyPayment }

        // Capacidade real de amortização baseada na renda (até 30% da renda da família)
        val capacityFromIncome = if (totalIncome > 0) totalIncome * 0.30 else 0.0
        val effectiveMonthlyCapacity = max(capacityFromIncome, sumMonthlyPayments)

        val isRestricted = totalIncome <= 0 && sumMonthlyPayments <= 0

        // Pagamento mensal recomendado:
        // Se a capacidade for positiva, é a própria capacidade efetiva. Se for restrita/zero, é 0.0.
        val recommendedPayment = if (isRestricted) 0.0 else effectiveMonthlyCapacity
        val extraPayment = max(0.0, effectiveMonthlyCapacity - sumMonthlyPayments)

        // Cálculo de meses para quitação com base na capacidade efetiva real
        val payoffMonths = when {
            totalDebt <= 0 -> 0
            effectiveMonthlyCapacity > 0 -> ceil(totalDebt / effectiveMonthlyCapacity).toInt()
            else -> 0 // Sem capacidade definida no momento
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
            extraPaymentCapacity = extraPayment,
            recommendedMonthlyPayment = recommendedPayment,
            payoffMonths = payoffMonths,
            highPriorityCount = highCount,
            mediumPriorityCount = medCount,
            lowPriorityCount = lowCount,
            sortedDebts = sortedDebts,
            isCapacityRestricted = isRestricted
        )
    }
}
