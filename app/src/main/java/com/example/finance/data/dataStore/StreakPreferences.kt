package com.example.finance.data.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.finance.domain.model.StreakData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.streakDataStore: DataStore<Preferences> by preferencesDataStore("finance_prefs")

object StreakKeys {
    val CURRENT_STREAK  = intPreferencesKey("current_streak")
    val LAST_LOGGED_DAY = longPreferencesKey("last_logged_day")
    val WEEK_HISTORY    = stringPreferencesKey("week_history")
}

class StreakPreferences(private val dataStore: DataStore<Preferences>) {

    val streakFlow: Flow<StreakData> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val streak  = prefs[StreakKeys.CURRENT_STREAK]  ?: 0
            val lastDay = prefs[StreakKeys.LAST_LOGGED_DAY] ?: 0L
            val history = prefs[StreakKeys.WEEK_HISTORY]    ?: "0000000"
            StreakData(
                currentStreak  = streak,
                lastLoggedDate = lastDay * 86_400_000L,
                weekHistory    = history.map { it == '1' }
            )
        }

    suspend fun recordActivity() {
        val todayEpochDay = System.currentTimeMillis() / 86_400_000L
        dataStore.edit { prefs ->
            val lastDay = prefs[StreakKeys.LAST_LOGGED_DAY] ?: 0L
            val current = prefs[StreakKeys.CURRENT_STREAK]  ?: 0
            val history = prefs[StreakKeys.WEEK_HISTORY]    ?: "0000000"
            val dayDiff = todayEpochDay - lastDay

            val newStreak = when {
                dayDiff == 0L -> current
                dayDiff == 1L -> current + 1
                else          -> 1
            }
            val newHistory = if (dayDiff == 0L) history else history.drop(1) + "1"

            prefs[StreakKeys.CURRENT_STREAK]  = newStreak
            prefs[StreakKeys.LAST_LOGGED_DAY] = todayEpochDay
            prefs[StreakKeys.WEEK_HISTORY]    = newHistory
        }
    }
}