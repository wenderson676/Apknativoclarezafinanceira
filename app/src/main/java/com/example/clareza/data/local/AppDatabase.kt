package com.example.clareza.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        GoalEntity::class,
        DebtEntity::class,
        MonthNoteEntity::class,
        UserSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clarezaDao(): ClarezaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clareza_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).clarezaDao()
                            // Seed default accounts
                            dao.insertAccounts(
                                listOf(
                                    AccountEntity(
                                        id = "banco",
                                        name = "Banco",
                                        icon = "🏦",
                                        type = "banco",
                                        initialBalance = 0.0,
                                        isMain = true
                                    ),
                                    AccountEntity(
                                        id = "reserva",
                                        name = "Reserva (Cofrinho)",
                                        icon = "💰",
                                        type = "reserva",
                                        initialBalance = 0.0,
                                        isMain = false
                                    ),
                                    AccountEntity(
                                        id = "carteira",
                                        name = "Carteira (Dinheiro Físico)",
                                        icon = "💵",
                                        type = "carteira",
                                        initialBalance = 0.0,
                                        isMain = false
                                    )
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
