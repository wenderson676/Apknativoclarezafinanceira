package com.example.clareza.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    val name: String,
    val icon: String = "🏦",
    val type: String = "custom", // "custom", "banco", "reserva", "carteira"
    val initialBalance: Double = 0.0,
    val isMain: Boolean = false
)

@Serializable
enum class TransactionType(val rawValue: String) {
    INCOME("income"),
    EXPENSE("expense"),
    TRANSFER_TO_SAVINGS("transfer_to_savings"),
    TRANSFER_FROM_SAVINGS("transfer_from_savings"),
    TRANSFER_BETWEEN_ACCOUNTS("transfer_between_accounts");

    companion object {
        fun fromString(value: String): TransactionType {
            return entries.find { it.rawValue == value || it.name.equals(value, ignoreCase = true) }
                ?: EXPENSE
        }
    }
}

@Serializable
data class Transaction(
    val id: String,
    val monthId: String,
    val amount: Double,
    val description: String,
    val bucket: String, // "Necessidades", "Desejos", "Reserva/Dívidas", "Renda", "Transferência"
    val category: String,
    val date: String, // "yyyy-MM-dd"
    val isPending: Boolean = false,
    val account: String = "banco",
    val toAccount: String? = null,
    val type: String = "expense"
)

@Serializable
data class Goal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0
)

@Serializable
data class Debt(
    val id: String,
    val name: String,
    val totalAmount: Double,
    val monthlyPayment: Double = 0.0,
    val interestRate: Double = 0.0,
    val isLate: Boolean = false,
    val creditor: String = "Não informado",
    val type: String = "other"
)

@Serializable
data class CustomCategories(
    val expense: List<String> = emptyList(),
    val income: List<String> = emptyList(),
    val transfer: List<String> = emptyList()
)

@Serializable
data class MonthlyData(
    val transactions: List<Transaction> = emptyList(),
    val devotionalNote: String = ""
)

@Serializable
data class AppState(
    val schemaVersion: Int = 1,
    val userName: String? = null,
    val budgetMode: String = "50-30-20",
    val themeMode: String = "light", // "light", "dark"
    val accounts: List<Account> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val monthlyData: Map<String, MonthlyData> = emptyMap(),
    val customCategories: CustomCategories = CustomCategories(),
    val dashboardCardOrder: List<String> = emptyList()
)
