package com.habitflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.presentation.navigation.NavGraph
import com.habitflow.app.presentation.theme.HabitFlowTheme
import com.habitflow.app.presentation.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val isLoading by authViewModel.isInitializing.collectAsState()

            splashScreen.setKeepOnScreenCondition { isLoading }

            HabitFlowTheme {
                if (!isLoading) {
                    NavGraph(startLoggedIn = isLoggedIn)
                }
            }
        }
    }
}
