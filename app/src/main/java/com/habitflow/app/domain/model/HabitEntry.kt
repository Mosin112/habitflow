package com.habitflow.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class HabitEntry(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val isCompleted: Boolean,
    val numericValue: Float?,
    val completedAt: LocalDateTime?
)
