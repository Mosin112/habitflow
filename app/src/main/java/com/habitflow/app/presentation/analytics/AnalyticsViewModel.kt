package com.habitflow.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.AnalyticsData
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitStat
import com.habitflow.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _analytics = MutableStateFlow<AnalyticsData?>(null)
    val analytics: StateFlow<AnalyticsData?> = _analytics.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            habitRepository.getAllHabits().collect { habits ->
                _isLoading.value = true
                val today = LocalDate.now()

                // Today's completion
                val todayHabits = habits.filter { today.dayOfWeek in it.frequency }
                val habitStats = habits.map { habit ->
                    computeHabitStat(habit)
                }

                // Weekly completion rates (Mon–Sun of current week)
                val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
                val weeklyRates = (0..6).map { offset ->
                    val date = weekStart.plusDays(offset.toLong())
                    val scheduled = habits.filter { date.dayOfWeek in it.frequency }
                    if (scheduled.isEmpty()) return@map 0f
                    val completed = scheduled.count { habit ->
                        val entries = habitRepository.getEntriesForHabit(habit.id)
                        entries.any { it.date == date && it.isCompleted }
                    }
                    completed.toFloat() / scheduled.size
                }

                val completedToday = todayHabits.count { habit ->
                    val entries = habitRepository.getEntriesForHabit(habit.id)
                    entries.any { it.date == today && it.isCompleted }
                }

                _analytics.value = AnalyticsData(
                    totalHabits = habits.size,
                    completedToday = completedToday,
                    todayCompletionRate = if (todayHabits.isEmpty()) 0f
                    else completedToday.toFloat() / todayHabits.size,
                    longestStreak = habitStats.maxOfOrNull { it.currentStreak } ?: 0,
                    weeklyCompletionRates = weeklyRates,
                    habitStats = habitStats
                )
                _isLoading.value = false
            }
        }
    }

    private suspend fun computeHabitStat(habit: Habit): HabitStat {
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)
        val entries = habitRepository.getEntriesForHabitInRange(
            habit.id, thirtyDaysAgo, today
        )

        // Scheduled days in last 30 days
        val scheduledDays = (0..29).count { offset ->
            val date = today.minusDays(offset.toLong())
            date.dayOfWeek in habit.frequency
        }
        val completedInPeriod = entries.count { it.isCompleted }
        val completionRate = if (scheduledDays == 0) 0f
        else completedInPeriod.toFloat() / scheduledDays

        // Current streak
        var currentStreak = 0
        var checkDate = today
        var firstCheck = true
        outer@ while (true) {
            if (checkDate.dayOfWeek in habit.frequency) {
                val entry = entries.find { it.date == checkDate }
                when {
                    entry?.isCompleted == true -> currentStreak++
                    firstCheck -> { /* skip today if not completed */ }
                    else -> break@outer
                }
            }
            firstCheck = false
            checkDate = checkDate.minusDays(1)
            if (today.toEpochDay() - checkDate.toEpochDay() > 60) break
        }

        // Best streak (simple: find longest run in last 30 days)
        var bestStreak = 0
        var runningStreak = 0
        val allEntries = habitRepository.getEntriesForHabit(habit.id)
        val entryMap = allEntries.associateBy { it.date }
        var date = today
        for (i in 0..364) {
            if (date.dayOfWeek in habit.frequency) {
                if (entryMap[date]?.isCompleted == true) {
                    runningStreak++
                    if (runningStreak > bestStreak) bestStreak = runningStreak
                } else {
                    runningStreak = 0
                }
            }
            date = date.minusDays(1)
        }

        return HabitStat(
            habit = habit,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            completionRate30Days = completionRate,
            totalCompletions = allEntries.count { it.isCompleted }
        )
    }
}
