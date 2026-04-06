package com.habitflow.app.presentation.navigation

sealed class Screen(val route: String) {

    // Auth flow
    object Login    : Screen("login")
    object SignUp   : Screen("signup")
    object Otp      : Screen("otp/{email}/{name}/{otp}") {
        fun createRoute(email: String, name: String, otp: String) = "otp/$email/$name/$otp"
    }

    // Main app (after login)
    object Home     : Screen("home")
    object Analytics : Screen("analytics")
    object Profile  : Screen("profile")

    // Habit management
    object AddHabit  : Screen("add_habit")
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: String) = "edit_habit/$habitId"
    }
}
