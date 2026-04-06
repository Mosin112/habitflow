package com.habitflow.app.data.local.mapper

import com.habitflow.app.data.local.entity.HabitEntity
import com.habitflow.app.data.local.entity.HabitEntryEntity
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitEntry
import com.habitflow.app.domain.model.HabitType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

// ─── Habit ────────────────────────────────────────────────────────────────────

fun HabitEntity.toDomain(
    streak: Int = 0,
    completionRate: Float = 0f,
    todayEntry: HabitEntry? = null
): Habit = Habit(
    id = id,
    name = name,
    type = HabitType.valueOf(type),
    frequency = frequency.split(",")
        .filter { it.isNotBlank() }
        .map { DayOfWeek.of(it.trim().toInt()) }
        .toSet(),
    goalValue = goalValue,
    unit = unit,
    colorHex = colorHex,
    icon = icon,
    createdAt = LocalDate.parse(createdAt),
    isActive = isActive,
    currentStreak = streak,
    completionRateLast30Days = completionRate,
    todayEntry = todayEntry
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    type = type.name,
    frequency = frequency.joinToString(",") { it.value.toString() },
    goalValue = goalValue,
    unit = unit,
    colorHex = colorHex,
    icon = icon,
    createdAt = createdAt.toString(),
    isActive = isActive
)

// ─── HabitEntry ───────────────────────────────────────────────────────────────

fun HabitEntryEntity.toDomain(): HabitEntry = HabitEntry(
    id = id,
    habitId = habitId,
    date = LocalDate.parse(date),
    isCompleted = isCompleted,
    numericValue = numericValue,
    completedAt = completedAt?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
    }
)

fun HabitEntry.toEntity(): HabitEntryEntity = HabitEntryEntity(
    id = id,
    habitId = habitId,
    date = date.toString(),
    isCompleted = isCompleted,
    numericValue = numericValue,
    completedAt = completedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
)
