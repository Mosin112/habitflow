package com.habitflow.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habitflow.app.presentation.analytics.AnalyticsScreen
import com.habitflow.app.presentation.auth.LoginScreen
import com.habitflow.app.presentation.auth.OtpScreen
import com.habitflow.app.presentation.auth.SignUpScreen
import com.habitflow.app.presentation.habit.AddEditHabitScreen
import com.habitflow.app.presentation.home.HomeScreen
import com.habitflow.app.presentation.profile.ProfileScreen
import com.habitflow.app.presentation.theme.DarkBg

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Today", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Analytics, "Insights", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

private val screensWithBottomBar = setOf(
    Screen.Home.route, Screen.Analytics.route, Screen.Profile.route
)

@Composable
fun NavGraph(startLoggedIn: Boolean) {
    val navController = rememberNavController()
    val startDestination = if (startLoggedIn) Screen.Home.route else Screen.Login.route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in screensWithBottomBar

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            if (showBottomBar) {
                HabitBottomBar(navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                slideInHorizontally(animationSpec = tween(300)) { it / 10 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                slideOutHorizontally(animationSpec = tween(300)) { it / 10 }
            }
        ) {
            // Auth flow
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignUpSuccess = { email, name, otp ->
                        navController.navigate(Screen.Otp.createRoute(email, name, otp))
                    }
                )
            }
            composable(
                route = Screen.Otp.route,
                arguments = listOf(
                    navArgument("email") { type = NavType.StringType },
                    navArgument("name")  { type = NavType.StringType },
                    navArgument("otp")   { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val email      = backStackEntry.arguments?.getString("email") ?: ""
                val name       = backStackEntry.arguments?.getString("name") ?: ""
                val displayOtp = backStackEntry.arguments?.getString("otp") ?: ""
                OtpScreen(
                    email = email,
                    name = name,
                    displayOtp = displayOtp,
                    onVerified = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Main app
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddHabit = { navController.navigate(Screen.AddHabit.route) },
                    onEditHabit = { habitId ->
                        navController.navigate(Screen.EditHabit.createRoute(habitId))
                    }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Habit management
            composable(Screen.AddHabit.route) {
                AddEditHabitScreen(
                    habitId = null,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EditHabit.route,
                arguments = listOf(navArgument("habitId") { type = NavType.StringType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                AddEditHabitScreen(
                    habitId = habitId,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun HabitBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = com.habitflow.app.presentation.theme.DarkSurface,
        tonalElevation = androidx.compose.ui.unit.Dp(0f)
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == item.screen.route
            } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = com.habitflow.app.presentation.theme.Indigo400,
                    selectedTextColor = com.habitflow.app.presentation.theme.Indigo400,
                    unselectedIconColor = com.habitflow.app.presentation.theme.TextTertiary,
                    unselectedTextColor = com.habitflow.app.presentation.theme.TextTertiary,
                    indicatorColor = com.habitflow.app.presentation.theme.Indigo800.copy(alpha = 0.3f)
                )
            )
        }
    }
}
