package com.habitflow.app.domain.model

enum class HabitType(val displayName: String, val description: String) {
    CHECK_IN("Check-in", "Mark as done or not done"),
    NUMERIC("Numeric", "Track a number (steps, km, glasses, etc.)")
}
