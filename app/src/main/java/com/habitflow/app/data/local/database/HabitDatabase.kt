package com.habitflow.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.HabitEntryDao
import com.habitflow.app.data.local.entity.HabitEntity
import com.habitflow.app.data.local.entity.HabitEntryEntity

@Database(
    entities = [HabitEntity::class, HabitEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao

    companion object {
        const val DATABASE_NAME = "habitflow.db"
    }
}
