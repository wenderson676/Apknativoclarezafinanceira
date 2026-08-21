package com.example.clareza.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.clareza.data.model.Account
import com.example.clareza.data.model.Debt
import com.example.clareza.data.model.Goal
import com.example.clareza.data.model.Transaction

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val initialBalance: Double,
    val isMain: Boolean
) {
    fun toModel() = Account(
        id = id,
        name = name,
        icon = icon,
        type = type,
        initialBalance = initialBalance,
        isMain = isMain
    )

    companion object {
        fun fromModel(model: Account) = AccountEntity(
            id = model.id,
            name = model.name,
            icon = model.icon,
            type = model.type,
            initialBalance = model.initialBalance,
            isMain = model.isMain
        )
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val monthId: String,
    val amount: Double,
    val description: String,
    val bucket: String,
    val category: String,
    val date: String,
    val isPending: Boolean,
    val account: String,
    val toAccount: String?,
    val type: String
) {
    fun toModel() = Transaction(
        id = id,
        monthId = monthId,
        amount = amount,
        description = description,
        bucket = bucket,
        category = category,
        date = date,
        isPending = isPending,
        account = account,
        toAccount = toAccount,
        type = type
    )

    companion object {
        fun fromModel(model: Transaction) = TransactionEntity(
            id = model.id,
            monthId = model.monthId,
            amount = model.amount,
            description = model.description,
            bucket = model.bucket,
            category = model.category,
            date = model.date,
            isPending = model.isPending,
            account = model.account,
            toAccount = model.toAccount,
            type = model.type
        )
    }
}

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double
) {
    fun toModel() = Goal(
        id = id,
        title = title,
        targetAmount = targetAmount,
        currentAmount = currentAmount
    )

    companion object {
        fun fromModel(model: Goal) = GoalEntity(
            id = model.id,
            title = model.title,
            targetAmount = model.targetAmount,
            currentAmount = model.currentAmount
        )
    }
}

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String,
    val name: String,
    val totalAmount: Double,
    val monthlyPayment: Double,
    val interestRate: Double,
    val isLate: Boolean,
    val creditor: String,
    val type: String
) {
    fun toModel() = Debt(
        id = id,
        name = name,
        totalAmount = totalAmount,
        monthlyPayment = monthlyPayment,
        interestRate = interestRate,
        isLate = isLate,
        creditor = creditor,
        type = type
    )

    companion object {
        fun fromModel(model: Debt) = DebtEntity(
            id = model.id,
            name = model.name,
            totalAmount = model.totalAmount,
            monthlyPayment = model.monthlyPayment,
            interestRate = model.interestRate,
            isLate = model.isLate,
            creditor = model.creditor,
            type = model.type
        )
    }
}

@Entity(tableName = "month_notes")
data class MonthNoteEntity(
    @PrimaryKey val monthId: String,
    val note: String
)

@Entity(tableName = "user_settings")
data class UserSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
