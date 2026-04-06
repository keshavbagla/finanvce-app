package com.example.finance.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val note: String,
    val timestamp: Long
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val emoji: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val createdAt: Long
)


@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp DESC")
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(txn: TransactionEntity)

    @Delete
    suspend fun delete(txn: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE type='INCOME' AND timestamp BETWEEN :start AND :end")
    suspend fun totalIncome(start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type='EXPENSE' AND timestamp BETWEEN :start AND :end")
    suspend fun totalExpense(start: Long, end: Long): Double?

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type='EXPENSE' AND timestamp BETWEEN :start AND :end GROUP BY category")
    suspend fun categoryBreakdown(start: Long, end: Long): List<CategoryTotal>
}

data class CategoryTotal(val category: String, val total: Double)

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt ASC")
    fun getAllFlow(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("UPDATE goals SET savedAmount = savedAmount + :amount WHERE id = :id")
    suspend fun addToGoal(id: String, amount: Double)
}


@Database(
    entities = [TransactionEntity::class, GoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao
}