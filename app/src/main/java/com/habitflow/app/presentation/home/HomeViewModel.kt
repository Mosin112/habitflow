package com.habitflow.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.preferences.SessionManager
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitEntry
import com.habitflow.app.domain.model.HabitType
import com.habitflow.app.domain.model.User
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.presentation.components.DayStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val habits: List<Habit> = emptyList(),
    val weekStatuses: List<DayStatus> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val completedCount: Int get() = habits.count { it.isCompletedToday }
    val totalCount: Int get() = habits.size
    val completionRate: Float get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    val greeting: String get() {
        val hour = java.time.LocalTime.now().hour
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _habits = _selectedDate
        .flatMapLatest { date -> habitRepository.getHabitsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _weekStatuses = MutableStateFlow<List<DayStatus>>(emptyList())

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        _habits,
        _weekStatuses,
        sessionManager.currentUser
    ) { date, habits, weekStatuses, user ->
        HomeUiState(
            selectedDate = date,
            habits = habits,
            weekStatuses = weekStatuses,
            currentUser = user
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState()
    )

    init {
        loadWeekStatuses()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleCheckIn(habit: Habit) {
        viewModelScope.launch {
            val today = _selectedDate.value
            val existing = habit.todayEntry
            if (existing != null && existing.isCompleted) {
                // Un-complete
                habitRepository.upsertEntry(
                    existing.copy(isCompleted = false, completedAt = null)
                )
            } else if (existing != null) {
                habitRepository.upsertEntry(
                    existing.copy(isCompleted = true, completedAt = LocalDateTime.now())
                )
            } else {
                habitRepository.upsertEntry(
                    HabitEntry(
                        id = UUID.randomUUID().toString(),
                        habitId = habit.id,
                        date = today,
                        isCompleted = true,
                        numericValue = null,
                        completedAt = LocalDateTime.now()
                    )
                )
            }
            loadWeekStatuses()
        }
    }

    fun updateNumeric(habit: Habit, value: Float) {
        viewModelScope.launch {
            val today = _selectedDate.value
            val isCompleted = habit.goalValue?.let { value >= it } ?: (value > 0)
            val existing = habit.todayEntry
            if (existing != null) {
                habitRepository.upsertEntry(
                    existing.copy(
                        numericValue = value,
                        isCompleted = isCompleted,
                        completedAt = if (isCompleted) LocalDateTime.now() else null
                    )
                )
            } else {
                habitRepository.upsertEntry(
                    HabitEntry(
                        id = UUID.randomUUID().toString(),
                        habitId = habit.id,
                        date = today,
                        isCompleted = isCompleted,
                        numericValue = value,
                        completedAt = if (isCompleted) LocalDateTime.now() else null
                    )
                )
            }
            loadWeekStatuses()
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
            loadWeekStatuses()
        }
    }

    private fun loadWeekStatuses() {
        viewModelScope.launch {
            val selected = _selectedDate.value
            val weekStart = selected.minusDays(
                (selected.dayOfWeek.value - 1).toLong()
            )
            val allHabits = habitRepository.getAllHabits().first()
            val statuses = (0..6).map { offset ->
                val date = weekStart.plusDays(offset.toLong())
                val scheduledHabits = allHabits.filter { habit ->
                    date.dayOfWeek in habit.frequency
                }
                val completedCount = scheduledHabits.count { habit ->
                    val entries = habitRepository.getEntriesForHabit(habit.id)
                    entries.any { it.date == date && it.isCompleted }
                }
                DayStatus(
                    date = date,
                    completionRate = if (scheduledHabits.isEmpty()) 0f
                    else completedCount.toFloat() / scheduledHabits.size
                )
            }
            _weekStatuses.value = statuses
        }
    }
}
