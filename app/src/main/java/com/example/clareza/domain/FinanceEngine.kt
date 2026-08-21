package com.example.clareza.domain

import com.example.clareza.data.model.Account
import com.example.clareza.data.model.Transaction
import com.example.clareza.ui.BucketProgress
import com.example.clareza.util.FinanceUtils

object FinanceEngine {

    /**
     * Calcula saldos individuais de cada conta e saldos agregados (Total, Líquido, Reservas)
     */
    fun calculateAccountBalances(
        accountsList: List<Account>,
        allTransactions: List<Transaction>
    ): Triple<Map<String, Double>, Double, Pair<Double, Double>> {
        val balances = mutableMapOf<String, Double>()
        accountsList.forEach { acc ->
            balances[acc.id] = acc.initialBalance
        }
        if (!balances.containsKey("banco")) balances["banco"] = 0.0
        if (!balances.containsKey("reserva")) balances["reserva"] = 0.0
        if (!balances.containsKey("carteira")) balances["carteira"] = 0.0

        allTransactions.filter { !it.isPending }.forEach { tx ->
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
                    val dest = toAct ?: "reserva"
                    balances[dest] = (balances[dest] ?: 0.0) + tx.amount
                }
                "transfer_from_savings" -> {
                    balances[act] = (balances[act] ?: 0.0) - tx.amount
                    val dest = toAct ?: "banco"
                    balances[dest] = (balances[dest] ?: 0.0) + tx.amount
                }
                "transfer_between_accounts" -> {
                    balances[act] = (balances[act] ?: 0.0) - tx.amount
                    if (toAct != null) {
                        balances[toAct] = (balances[toAct] ?: 0.0) + tx.amount
                    }
                }
            }
        }

        val totalBalance = balances.values.sum()

        // Distinção de saldos líquidos vs reservas
        val savingsAccounts = accountsList.filter { it.type == "reserva" || it.id == "reserva" }.map { it.id }.toSet()
        val savingsBalance = balances.filter { savingsAccounts.contains(it.key) || it.key == "reserva" }.values.sum()
        val liquidBalance = totalBalance - savingsBalance

        return Triple(balances, totalBalance, Pair(liquidBalance, savingsBalance))
    }

    /**
     * Calcula o progresso dos potes de orçamento (ex: 50/30/20) para o mês corrente
     */
    fun calculateBucketProgress(
        currentMonthTxs: List<Transaction>,
        totalIncome: Double,
        liquidBalance: Double,
        budgetMode: String
    ): List<BucketProgress> {
        val modeInfo = FinanceUtils.BUDGET_MODES_INFO[budgetMode] ?: FinanceUtils.BUDGET_MODES_INFO["50-30-20"]!!
        val referenceIncome = if (totalIncome > 0) totalIncome else 0.0

        val needsSpent = currentMonthTxs
            .filter { it.type == "expense" && it.bucket == "Necessidades" && !it.isPending }
            .sumOf { it.amount }

        val wantsSpent = currentMonthTxs
            .filter { it.type == "expense" && it.bucket == "Desejos" && !it.isPending }
            .sumOf { it.amount }

        val savingsSpent = currentMonthTxs
            .filter { !it.isPending && ((it.type == "expense" && it.bucket == "Reserva/Dívidas") || it.type == "transfer_to_savings") }
            .sumOf { it.amount }

        fun makeProgress(name: String, spent: Double, ratio: Double): BucketProgress {
            val limit = referenceIncome * ratio
            val pct = if (referenceIncome > 0) (spent / referenceIncome) * 100 else if (spent > 0) 100.0 else 0.0
            val status = when {
                limit <= 0 && spent > 0 -> "danger"
                limit <= 0 -> "normal"
                spent > limit -> "danger"
                spent > limit * 0.85 -> "warning"
                else -> "normal"
            }
            return BucketProgress(
                bucketName = name,
                spent = spent,
                limit = limit,
                ratio = ratio,
                percentage = pct,
                status = status
            )
        }

        return listOf(
            makeProgress("Necessidades", needsSpent, modeInfo.ratios["Necessidades"] ?: 0.50),
            makeProgress("Desejos", wantsSpent, modeInfo.ratios["Desejos"] ?: 0.30),
            makeProgress("Reserva/Dívidas", savingsSpent, modeInfo.ratios["Reserva/Dívidas"] ?: 0.20)
        )
    }
}
