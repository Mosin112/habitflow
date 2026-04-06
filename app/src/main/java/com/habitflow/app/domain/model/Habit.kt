package com.habitflow.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class Habit(
    val id: String,
    val name: String,
    val type: HabitType,
    val frequency: Set<DayOfWeek>,      // which days this habit is scheduled
    val goalValue: Float?,               // for NUMERIC type
    val unit: String?,                   // e.g. "glasses", "km", "steps"
    val colorHex: String,               // "#6366F1"
    val icon: String,                   // emoji or icon name, e.g. "🏃"
    val createdAt: LocalDate,
    val isActive: Boolean,
    // Derived/computed fields (populated from entries)
    val currentStreak: Int = 0,
    val completionRateLast30Days: Float = 0f,
    val todayEntry: HabitEntry? = null
) {
    val isScheduledForDate: (LocalDate) -> Boolean = { date ->
        date.dayOfWeek in frequency
    }

    val isCompletedToday: Boolean
        get() = todayEntry?.isCompleted == true

    val todayNumericValue: Float?
        get() = todayEntry?.numericValue

    val progressToGoal: Float
        get() {
            if (type != HabitType.NUMERIC || goalValue == null || goalValue <= 0f) return 0f
            val value = todayNumericValue ?: 0f
            return (value / goalValue).coerceIn(0f, 1f)
        }
}
