package com.habitflow.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.presentation.theme.*

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (email: String, name: String, otp: String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val passwordMismatch = confirmPassword.isNotBlank() && password != confirmPassword
    val canSubmit = name.isNotBlank() && email.isNotBlank() &&
            password.length >= 6 && !passwordMismatch && !uiState.isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.habitflow.app.presentation.theme.DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // Back button + title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Create account",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            }
            Text(
                "Join thousands building better habits",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 48.dp, top = 4.dp).fillMaxWidth()
            )

            Spacer(Modifier.height(40.dp))

            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                leadingIcon = { Icon(Icons.Default.Person, null, tint = TextTertiary) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(Modifier.height(16.dp))
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = TextTertiary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(Modifier.height(16.dp))
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password (min. 6 chars)",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextTertiary) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = TextTertiary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(Modifier.height(16.dp))
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextTertiary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (canSubmit) viewModel.signUp(name, email, password) { e, n, otp -> onSignUpSuccess(e, n, otp) }
                })
            )
            if (passwordMismatch) {
                Text(
                    "Passwords do not match",
                    color = Rose400,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp).fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = uiState.error != null, enter = fadeIn(), exit = fadeOut()) {
                uiState.error?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        color = Rose500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            error,
                            color = Rose400,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.signUp(name, email, password) { e, n, otp -> onSignUpSuccess(e, n, otp) }
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Create Account", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign in", color = Indigo400, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
