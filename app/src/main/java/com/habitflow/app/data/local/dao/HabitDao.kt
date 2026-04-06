package com.habitflow.app.data.local.dao

import androidx.room.*
import com.habitflow.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteHabit(id: String)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String)
}
