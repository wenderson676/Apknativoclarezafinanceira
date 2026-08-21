package com.example.clareza.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction as RoomTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ClarezaDao {

    // Accounts
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: String)

    // Transactions
    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE monthId = :monthId ORDER BY date DESC")
    fun getTransactionsForMonth(monthId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("UPDATE transactions SET isPending = :isPending WHERE id = :id")
    suspend fun updateTransactionPending(id: String, isPending: Boolean)

    // Goals
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: String)

    // Debts
    @Query("SELECT * FROM debts")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebt(id: String)

    // Month notes
    @Query("SELECT * FROM month_notes")
    fun getAllMonthNotes(): Flow<List<MonthNoteEntity>>

    @Query("SELECT note FROM month_notes WHERE monthId = :monthId")
    suspend fun getNoteForMonth(monthId: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthNote(note: MonthNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthNotes(notes: List<MonthNoteEntity>)

    // User settings
    @Query("SELECT * FROM user_settings")
    fun getAllSettings(): Flow<List<UserSettingEntity>>

    @Query("SELECT value FROM user_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: UserSettingEntity)

    // Clear all
    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM goals")
    suspend fun clearGoals()

    @Query("DELETE FROM debts")
    suspend fun clearDebts()

    @Query("DELETE FROM month_notes")
    suspend fun clearMonthNotes()

    @Query("DELETE FROM user_settings")
    suspend fun clearSettings()

    @RoomTransaction
    suspend fun clearAllData() {
        clearAccounts()
        clearTransactions()
        clearGoals()
        clearDebts()
        clearMonthNotes()
        clearSettings()
    }
}
