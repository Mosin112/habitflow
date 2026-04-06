package com.habitflow.app.data.local.dao

import androidx.room.*
import com.habitflow.app.data.local.entity.HabitEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEntryDao {

    @Query("SELECT * FROM habit_entries WHERE date = :date")
    fun getEntriesForDate(date: String): Flow<List<HabitEntryEntity>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getEntriesForHabit(habitId: String): List<HabitEntryEntity>

    @Query("""
        SELECT * FROM habit_entries
        WHERE habitId = :habitId AND date >= :startDate AND date <= :endDate
        ORDER BY date DESC
    """)
    suspend fun getEntriesForHabitInRange(
        habitId: String,
        startDate: String,
        endDate: String
    ): List<HabitEntryEntity>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getEntryForHabitOnDate(habitId: String, date: String): HabitEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: HabitEntryEntity)

    @Query("DELETE FROM habit_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId")
    suspend fun deleteAllEntriesForHabit(habitId: String)

    @Query("""
        SELECT COUNT(*) FROM habit_entries
        WHERE habitId = :habitId AND isCompleted = 1
    """)
    suspend fun getTotalCompletionsForHabit(habitId: String): Int

    @Query("""
        SELECT * FROM habit_entries
        WHERE habitId = :habitId AND date >= :startDate
        ORDER BY date DESC
    """)
    suspend fun getEntriesSince(habitId: String, startDate: String): List<HabitEntryEntity>
}
