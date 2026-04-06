package com.habitflow.app.data.repository

import com.habitflow.app.data.mock.MockOtpService
import com.habitflow.app.data.preferences.SessionManager
import com.habitflow.app.domain.model.User
import com.habitflow.app.domain.repository.AuthRepository
import com.habitflow.app.util.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val otpService: MockOtpService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun signUp(name: String, email: String, password: String): Result<String> {
        delay(800) // Simulate network call
        return try {
            if (name.isBlank()) return Result.Error("Name cannot be empty")
            if (!email.contains("@")) return Result.Error("Invalid email address")
            if (password.length < 6) return Result.Error("Password must be at least 6 characters")
            if (otpService.userExists(email)) return Result.Error("An account with this email already exists")

            val otp = otpService.storePendingUser(name, email, password)
            Result.Success(otp) // In prod: OTP sent via email, not returned
        } catch (e: Exception) {
            Result.Error("Sign up failed. Please try again.", e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        delay(800) // Simulate network call
        return try {
            if (!email.contains("@")) return Result.Error("Invalid email address")
            if (password.isBlank()) return Result.Error("Password cannot be empty")

            val mockUser = otpService.validateLogin(email, password)
                ?: return Result.Error("Incorrect email or password")

            val user = User(
                id = mockUser.id,
                name = mockUser.name,
                email = mockUser.email
            )
            sessionManager.saveSession(user)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error("Login failed. Please try again.", e)
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): Result<User> {
        delay(600)
        return try {
            if (otp.length != 6) return Result.Error("OTP must be 6 digits")

            val mockUser = otpService.verifyOtp(email, otp)
                ?: return Result.Error("Invalid or expired OTP. Please try again.")

            val user = User(
                id = mockUser.id,
                name = mockUser.name,
                email = mockUser.email
            )
            sessionManager.saveSession(user)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error("Verification failed. Please try again.", e)
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override fun getCurrentUser(): Flow<User?> = sessionManager.currentUser

    override fun isLoggedIn(): Boolean = false // Sync check; use Flow for reactive
}
