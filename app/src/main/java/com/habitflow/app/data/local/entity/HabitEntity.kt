package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,           // "CHECK_IN" | "NUMERIC"
    val frequency: String,      // comma-separated DayOfWeek values "1,2,3,4,5"
    val goalValue: Float?,
    val unit: String?,
    val colorHex: String,
    val icon: String,
    val createdAt: String,      // "yyyy-MM-dd"
    val isActive: Boolean
)
