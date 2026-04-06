package com.habitflow.app.data.mock

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock OTP service that simulates email-based verification.
 * In production, replace with a real OTP delivery service (Firebase, Twilio, etc.).
 *
 * For demo purposes, the generated OTP is returned to the UI so the user can see it
 * in a "Check your email" screen — mimicking what would arrive in their inbox.
 */
@Singleton
class MockOtpService @Inject constructor() {

    private val otpStore = mutableMapOf<String, String>()
    private val userStore = mutableMapOf<String, MockUser>() // email → user data

    data class MockUser(
        val id: String,
        val name: String,
        val email: String,
        val passwordHash: String   // in prod: use proper hashing (bcrypt, etc.)
    )

    /** Store a new user record (used during sign-up before OTP verification). */
    fun storePendingUser(name: String, email: String, password: String): String {
        val otp = (100000..999999).random().toString()
        otpStore[email] = otp
        userStore[email] = MockUser(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            email = email,
            passwordHash = password.hashCode().toString() // simplistic for demo
        )
        return otp
    }

    fun verifyOtp(email: String, otp: String): MockUser? {
        return if (otpStore[email] == otp) {
            otpStore.remove(email)
            userStore[email]
        } else null
    }

    fun validateLogin(email: String, password: String): MockUser? {
        val user = userStore[email] ?: return null
        return if (user.passwordHash == password.hashCode().toString()) user else null
    }

    fun userExists(email: String): Boolean = userStore.containsKey(email)

    fun invalidateOtp(email: String) {
        otpStore.remove(email)
    }
}
