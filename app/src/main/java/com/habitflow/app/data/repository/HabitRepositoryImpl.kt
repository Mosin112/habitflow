package com.habitflow.app.data.repository

import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.HabitEntryDao
import com.habitflow.app.data.local.mapper.toDomain
import com.habitflow.app.data.local.mapper.toEntity
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitEntry
import com.habitflow.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val entryDao: HabitEntryDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> =
        habitDao.getAllActiveHabits().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getHabitById(id: String): Habit? =
        habitDao.getHabitById(id)?.toDomain()

    override suspend fun insertHabit(habit: Habit) =
        habitDao.insertHabit(habit.toEntity())

    override suspend fun updateHabit(habit: Habit) =
        habitDao.insertHabit(habit.toEntity()) // upsert

    override suspend fun deleteHabit(habitId: String) =
        habitDao.deleteHabit(habitId)

    override fun getEntriesForDate(date: LocalDate): Flow<List<HabitEntry>> =
        entryDao.getEntriesForDate(date.toString()).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getEntriesForHabit(habitId: String): List<HabitEntry> =
        entryDao.getEntriesForHabit(habitId).map { it.toDomain() }

    override suspend fun getEntriesForHabitInRange(
        habitId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitEntry> =
        entryDao.getEntriesForHabitInRange(
            habitId,
            startDate.toString(),
            endDate.toString()
        ).map { it.toDomain() }

    override suspend fun upsertEntry(entry: HabitEntry) =
        entryDao.upsertEntry(entry.toEntity())

    override suspend fun deleteEntry(entryId: String) =
        entryDao.deleteEntry(entryId)

    override fun getHabitsForDate(date: LocalDate): Flow<List<Habit>> {
        return combine(
            habitDao.getAllActiveHabits(),
            entryDao.getEntriesForDate(date.toString())
        ) { habitEntities, entryEntities ->
            val entryMap = entryEntities.associateBy { it.habitId }
            val filtered = habitEntities.filter { entity ->
                // Only show habits scheduled for this day of week
                val frequency = entity.frequency.split(",")
                    .filter { it.isNotBlank() }
                    .map { DayOfWeek.of(it.trim().toInt()) }
                    .toSet()
                date.dayOfWeek in frequency
            }
            // Use a for loop (not map) so we can call the suspend calculateStreak
            val result = mutableListOf<Habit>()
            for (entity in filtered) {
                val todayEntry = entryMap[entity.id]?.toDomain()
                val streak = calculateStreak(entity.id, entity.frequency, date)
                result.add(entity.toDomain(streak = streak, todayEntry = todayEntry))
            }
            result as List<Habit>
        }
    }

    // ─── Streak Calculation ────────────────────────────────────────────────────

    private suspend fun calculateStreak(
        habitId: String,
        frequencyStr: String,
        asOfDate: LocalDate
    ): Int {
        val frequency = frequencyStr.split(",")
            .filter { it.isNotBlank() }
            .map { DayOfWeek.of(it.trim().toInt()) }
            .toSet()

        val startDate = asOfDate.minusDays(365)
        val entries = entryDao.getEntriesForHabitInRange(
            habitId,
            startDate.toString(),
            asOfDate.toString()
        ).map { it.toDomain() }
            .associateBy { it.date }

        var streak = 0
        var checkDate = asOfDate
        var firstCheck = true

        while (true) {
            if (checkDate.dayOfWeek in frequency) {
                val entry = entries[checkDate]
                when {
                    entry?.isCompleted == true -> streak++
                    firstCheck -> {
                        // Today/current date not yet completed — look back further
                    }
                    else -> break // Past scheduled day was missed — streak broken
                }
            }
            firstCheck = false
            checkDate = checkDate.minusDays(1)
            if (asOfDate.toEpochDay() - checkDate.toEpochDay() > 366) break
        }
        return streak
    }
}
