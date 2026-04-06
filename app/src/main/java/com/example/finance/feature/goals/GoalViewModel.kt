package com.example.finance.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance.data.dataStore.StreakPreferences
import com.example.finance.domain.model.*
import com.example.finance.domain.repository.IGoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val streak: StreakData? = null,
    val weeklyChallenge: WeeklyChallenge? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepo: IGoalRepository,
    private val streakPrefs: StreakPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(GoalsUiState())
    val state: StateFlow<GoalsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(goalRepo.getAll(), streakPrefs.streakFlow) { goals, streak ->
                goals to streak
            }.collect { (goals, streak) ->
                _state.update {
                    it.copy(
                        goals     = goals,
                        streak    = streak,
                        isLoading = false,
                        weeklyChallenge = generateChallenge()
                    )
                }
            }
        }
    }

    fun addGoal(goal: Goal) = viewModelScope.launch { goalRepo.upsert(goal) }
    fun deleteGoal(goal: Goal) = viewModelScope.launch { goalRepo.delete(goal) }
    fun addSavings(id: String, amount: Double) = viewModelScope.launch { goalRepo.addSavings(id, amount) }

    private fun generateChallenge(): WeeklyChallenge {
        val weekEnd = System.currentTimeMillis() + 3 * 86_400_000L
        return WeeklyChallenge(
            id           = "challenge_food",
            description  = "Spend under ₹1,500 on Food this week",
            category     = Category.FOOD,
            spendLimit   = 1500.0,
            currentSpend = 840.0,
            expiresAt    = weekEnd
        )
    }
}
