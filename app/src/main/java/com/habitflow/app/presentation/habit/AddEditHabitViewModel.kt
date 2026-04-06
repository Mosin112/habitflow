package com.habitflow.app.presentation.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitType
import com.habitflow.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class AddEditHabitState(
    val name: String = "",
    val type: HabitType = HabitType.CHECK_IN,
    val frequency: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    ),
    val goalValue: String = "",
    val unit: String = "",
    val selectedColor: String = "#6366F1",
    val selectedIcon: String = "✅",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
) {
    val isValid: Boolean get() = name.isNotBlank() && frequency.isNotEmpty()
}

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditHabitState())
    val state: StateFlow<AddEditHabitState> = _state.asStateFlow()

    private var editingHabitId: String? = null

    fun loadHabit(habitId: String) {
        viewModelScope.launch {
            val habit = habitRepository.getHabitById(habitId) ?: return@launch
            editingHabitId = habitId
            _state.value = AddEditHabitState(
                name = habit.name,
                type = habit.type,
                frequency = habit.frequency,
                goalValue = habit.goalValue?.toString() ?: "",
                unit = habit.unit ?: "",
                selectedColor = habit.colorHex,
                selectedIcon = habit.icon,
                isEditMode = true
            )
        }
    }

    fun updateName(name: String) { _state.value = _state.value.copy(name = name) }
    fun updateType(type: HabitType) { _state.value = _state.value.copy(type = type) }
    fun updateGoalValue(value: String) { _state.value = _state.value.copy(goalValue = value) }
    fun updateUnit(unit: String) { _state.value = _state.value.copy(unit = unit) }
    fun updateColor(color: String) { _state.value = _state.value.copy(selectedColor = color) }
    fun updateIcon(icon: String) { _state.value = _state.value.copy(selectedIcon = icon) }

    fun toggleDay(day: DayOfWeek) {
        val current = _state.value.frequency.toMutableSet()
        if (day in current) {
            if (current.size > 1) current.remove(day) // Keep at least 1 day
        } else {
            current.add(day)
        }
        _state.value = _state.value.copy(frequency = current)
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            if (!s.isValid) {
                _state.value = s.copy(error = "Please fill in all required fields")
                return@launch
            }

            _state.value = s.copy(isLoading = true, error = null)
            try {
                val habit = Habit(
                    id = editingHabitId ?: UUID.randomUUID().toString(),
                    name = s.name.trim(),
                    type = s.type,
                    frequency = s.frequency,
                    goalValue = s.goalValue.toFloatOrNull(),
                    unit = s.unit.trim().takeIf { it.isNotBlank() },
                    colorHex = s.selectedColor,
                    icon = s.selectedIcon,
                    createdAt = LocalDate.now(),
                    isActive = true
                )
                habitRepository.insertHabit(habit)
                _state.value = s.copy(isSaved = true)
                onSuccess()
            } catch (e: Exception) {
                _state.value = s.copy(isLoading = false, error = "Failed to save. Please try again.")
            }
        }
    }
}
