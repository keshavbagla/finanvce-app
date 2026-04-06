package com.example.finance.data.repository

import com.example.finance.data.local.*
import com.example.finance.domain.model.*
import com.example.finance.domain.repository.IGoalRepository
import com.example.finance.domain.repository.ITransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : ITransactionRepository {

    override fun getAll(): Flow<List<Transaction>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override fun getByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>> =
        dao.getByDateRange(startMs, endMs).map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(txn: Transaction) = dao.upsert(txn.toEntity())

    override suspend fun delete(txn: Transaction) = dao.delete(txn.toEntity())

    override suspend fun getMonthlyStats(startMs: Long, endMs: Long): MonthlyStats {
        val income   = dao.totalIncome(startMs, endMs)  ?: 0.0
        val expense  = dao.totalExpense(startMs, endMs) ?: 0.0
        val breakdown = dao.categoryBreakdown(startMs, endMs)
            .associate { Category.valueOf(it.category) to it.total }
        return MonthlyStats(income, expense, income - expense, breakdown)
    }
}

class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao
) : IGoalRepository {
    override fun getAll(): Flow<List<Goal>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }
    override suspend fun upsert(goal: Goal) = dao.upsert(goal.toEntity())
    override suspend fun delete(goal: Goal) = dao.delete(goal.toEntity())
    override suspend fun addSavings(id: String, amount: Double) = dao.addToGoal(id, amount)
}
