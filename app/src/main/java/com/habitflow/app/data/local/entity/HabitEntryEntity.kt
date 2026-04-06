package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_entries",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("date")]
)
data class HabitEntryEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val date: String,           // "yyyy-MM-dd"
    val isCompleted: Boolean,
    val numericValue: Float?,
    val completedAt: Long?      // epoch millis
)
