package com.example.finance.domain.model

import java.util.UUID

enum class TransactionType { INCOME, EXPENSE }

enum class Category(val emoji: String, val label: String) {
    FOOD("🍔", "Food & Dining"),
    TRANSPORT("🚗", "Transport"),
    SHOPPING("🛒", "Shopping"),
    BILLS("💡", "Bills & Utilities"),
    HEALTH("💊", "Health"),
    ENTERTAINMENT("🎮", "Entertainment"),
    SALARY("💰", "Salary"),
    FREELANCE("💸", "Freelance"),
    INVESTMENT("📈", "Investment"),
    OTHER("📌", "Other")
}

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val emoji: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progress: Double get() = (savedAmount / targetAmount).coerceIn(0.0, 1.0)
    val progressPercent: Int get() = (progress * 100).toInt()
}

data class MonthlyStats(
    val totalIncome: Double,
    val totalExpense: Double,
    val net: Double = totalIncome - totalExpense,
    val categoryBreakdown: Map<Category, Double>
)

data class StreakData(
    val currentStreak: Int,
    val lastLoggedDate: Long,
    val weekHistory: List<Boolean>
)

data class WeeklyChallenge(
    val id: String,
    val description: String,
    val category: Category?,
    val spendLimit: Double,
    val currentSpend: Double,
    val expiresAt: Long
) {
    val isCompleted: Boolean get() = currentSpend <= spendLimit
    val daysRemaining: Int get() =
        ((expiresAt - System.currentTimeMillis()) / 86_400_000L).toInt().coerceAtLeast(0)
}