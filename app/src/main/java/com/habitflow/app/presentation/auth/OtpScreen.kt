package com.habitflow.app.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.presentation.theme.*

@Composable
fun OtpScreen(
    email: String,
    name: String,
    displayOtp: String = "",    // Demo: shows the OTP so user can enter it
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var otpValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Row(Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Email icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Indigo800.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = Indigo400,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Check your email",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "We sent a 6-digit code to\n$email",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            // Demo hint — shows the OTP since this is a mock backend
            if (displayOtp.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Amber500.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔑 ", fontSize = 16.sp)
                        Text(
                            "Demo mode — your OTP: $displayOtp",
                            style = MaterialTheme.typography.labelMedium,
                            color = Amber400
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // OTP input — hidden text field + 6 visible boxes
            Box(contentAlignment = Alignment.Center) {
                // Invisible text field that captures input
                OutlinedTextField(
                    value = otpValue,
                    onValueChange = { value ->
                        if (value.length <= 6 && value.all { it.isDigit() }) {
                            otpValue = value
                        }
                    },
                    modifier = Modifier
                        .size(1.dp) // invisible
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // Visual OTP boxes
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(6) { index ->
                        val char = otpValue.getOrNull(index)?.toString() ?: ""
                        val isFocused = index == otpValue.length
                        OtpBox(char = char, isFocused = isFocused)
                    }
                }
            }

            // Error
            AnimatedVisibility(visible = uiState.error != null, enter = fadeIn(), exit = fadeOut()) {
                uiState.error?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        color = Rose500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            error, color = Rose400,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.verifyOtp(email, otpValue) { onVerified() } },
                enabled = otpValue.length == 6 && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Verify & Continue", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = {
                // In prod: re-send OTP
            }) {
                Text("Resend code", color = Indigo400, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun OtpBox(char: String, isFocused: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 56.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Indigo500 else DarkBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        if (isFocused && char.isEmpty()) {
            // Blinking cursor
            Text("|", style = MaterialTheme.typography.titleLarge, color = Indigo400)
        }
    }
}
