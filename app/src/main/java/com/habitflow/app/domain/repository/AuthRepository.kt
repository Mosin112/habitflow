package com.habitflow.app.domain.repository

import com.habitflow.app.domain.model.User
import com.habitflow.app.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(name: String, email: String, password: String): Result<String> // Returns generated OTP (for demo)
    suspend fun login(email: String, password: String): Result<User>
    suspend fun verifyOtp(email: String, otp: String): Result<User>
    suspend fun logout()
    fun getCurrentUser(): Flow<User?>
    fun isLoggedIn(): Boolean
}
