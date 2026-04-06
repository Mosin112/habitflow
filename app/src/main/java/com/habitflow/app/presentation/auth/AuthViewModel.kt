package com.habitflow.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.preferences.SessionManager
import com.habitflow.app.domain.repository.AuthRepository
import com.habitflow.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val generatedOtp: String? = null // For demo: shows the OTP the "email" would contain
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            // Let the session check complete before hiding splash
            sessionManager.isLoggedIn.collect {
                _isInitializing.value = false
            }
        }
    }

    fun signUp(name: String, email: String, password: String, onSuccess: (String, String, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signUp(name, email, password)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState(generatedOtp = result.data)
                    onSuccess(email, name, result.data)
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun verifyOtp(email: String, otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.verifyOtp(email, otp)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
