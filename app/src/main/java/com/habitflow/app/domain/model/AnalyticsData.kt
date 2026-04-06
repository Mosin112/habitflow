package com.habitflow.app.domain.model

data class AnalyticsData(
    val totalHabits: Int,
    val completedToday: Int,
    val todayCompletionRate: Float,
    val longestStreak: Int,
    val weeklyCompletionRates: List<Float>, // 7 values, index 0 = Monday
    val habitStats: List<HabitStat>
)

data class HabitStat(
    val habit: Habit,
    val currentStreak: Int,
    val bestStreak: Int,
    val completionRate30Days: Float,
    val totalCompletions: Int
)
