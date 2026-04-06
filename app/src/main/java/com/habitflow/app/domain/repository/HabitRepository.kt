package com.habitflow.app.domain.repository

import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {

    // Habits CRUD
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: String): Habit?
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)

    // Entries
    fun getEntriesForDate(date: LocalDate): Flow<List<HabitEntry>>
    suspend fun getEntriesForHabit(habitId: String): List<HabitEntry>
    suspend fun getEntriesForHabitInRange(
        habitId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitEntry>
    suspend fun upsertEntry(entry: HabitEntry)
    suspend fun deleteEntry(entryId: String)

    // Convenience: get habits with today's entries baked in
    fun getHabitsForDate(date: LocalDate): Flow<List<Habit>>
}
