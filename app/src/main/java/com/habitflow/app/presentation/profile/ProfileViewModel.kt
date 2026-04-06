package com.habitflow.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.preferences.SessionManager
import com.habitflow.app.domain.model.User
import com.habitflow.app.domain.repository.AuthRepository
import com.habitflow.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val totalHabits: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        sessionManager.currentUser,
        habitRepository.getAllHabits()
    ) { user, habits ->
        ProfileUiState(user = user, totalHabits = habits.size)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProfileUiState(isLoading = true)
    )

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
