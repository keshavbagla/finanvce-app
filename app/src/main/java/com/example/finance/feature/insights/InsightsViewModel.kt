package com.example.finance.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class TimePeriod(val label: String) { WEEK("1W"), MONTH("1M"), THREE_MONTHS("3M") }

data class InsightsUiState(
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val isLoading: Boolean = false,
    val monthlyStats: MonthlyStats? = null,
    val topCategories: List<Pair<Category, Double>> = emptyList()
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    fun setPeriod(period: TimePeriod) {
        _state.update { it.copy(selectedPeriod = period) }
        // Fetch data based on period
    }
}
