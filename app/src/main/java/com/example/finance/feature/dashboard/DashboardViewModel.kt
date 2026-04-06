package com.example.finance.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance.domain.model.*
import com.example.finance.domain.repository.ITransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val balance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val weeklySpending: List<Double> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val txnRepo: ITransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        observeData()
    }

    // ✅ NEW FUNCTION - this is what DashboardScreen calls when user saves a transaction
    fun addTransaction(txn: Transaction) {
        viewModelScope.launch {
            txnRepo.upsert(txn)
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            txnRepo.getAll().collect { transactions ->
                val now        = System.currentTimeMillis()
                val monthStart = getMonthStart()

                val income  = transactions.filter {
                    it.type == TransactionType.INCOME && it.timestamp >= monthStart
                }.sumOf { it.amount }

                val expense = transactions.filter {
                    it.type == TransactionType.EXPENSE && it.timestamp >= monthStart
                }.sumOf { it.amount }

                val allIncome  = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val allExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                val weeklyData = (0..6).map { dayOffset ->
                    val dayStart = getDayStart(now - (6 - dayOffset) * 86_400_000L)
                    val dayEnd   = dayStart + 86_400_000L
                    transactions.filter {
                        it.type == TransactionType.EXPENSE &&
                                it.timestamp in dayStart until dayEnd
                    }.sumOf { it.amount }
                }

                _state.update {
                    it.copy(
                        balance            = allIncome - allExpense,
                        monthlyIncome      = income,
                        monthlyExpense     = expense,
                        recentTransactions = transactions.take(10),
                        weeklySpending     = weeklyData,
                        isLoading          = false
                    )
                }
            }
        }
    }

    private fun getMonthStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getDayStart(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}