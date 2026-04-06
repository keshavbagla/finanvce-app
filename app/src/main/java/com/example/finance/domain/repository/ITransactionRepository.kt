package com.example.finance.domain.repository


import com.example.finance.domain.model.Goal
import com.example.finance.domain.model.MonthlyStats
import com.example.finance.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface ITransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>>
    suspend fun upsert(txn: Transaction)
    suspend fun delete(txn: Transaction)
    suspend fun getMonthlyStats(startMs: Long, endMs: Long): MonthlyStats
}

interface IGoalRepository {
    fun getAll(): Flow<List<Goal>>
    suspend fun upsert(goal: Goal)
    suspend fun delete(goal: Goal)
    suspend fun addSavings(id: String, amount: Double)
}