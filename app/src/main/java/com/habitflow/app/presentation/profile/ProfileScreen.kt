package com.habitflow.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.presentation.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = DarkCard,
            title = { Text("Sign Out?", color = TextPrimary) },
            text = { Text("Are you sure you want to sign out?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout { onLogout() }
                }) { Text("Sign Out", color = Rose400) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.statusBarsPadding())
            Spacer(Modifier.height(20.dp))

            Text(
                "Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            // Avatar
            uiState.user?.let { user ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Indigo500, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.avatarInitials,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    user.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Stats card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkCard,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBlock("${uiState.totalHabits}", "Active Habits", "📋")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Settings list
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkCard,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ProfileRow(
                        icon = Icons.Default.Notifications,
                        label = "Reminders",
                        subtitle = "Daily habit reminders",
                        tint = Indigo400
                    )
                    HorizontalDivider(color = DarkDivider, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRow(
                        icon = Icons.Default.Palette,
                        label = "Appearance",
                        subtitle = "Dark mode (default)",
                        tint = Violet400
                    )
                    HorizontalDivider(color = DarkDivider, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRow(
                        icon = Icons.Default.Info,
                        label = "About",
                        subtitle = "HabitFlow v1.0.0",
                        tint = Emerald400
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Logout
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose400),
                border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Logout, null, tint = Rose400, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", color = Rose400, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(tint.copy(0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}
