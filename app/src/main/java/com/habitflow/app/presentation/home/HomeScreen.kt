package com.habitflow.app.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.presentation.components.*
import com.habitflow.app.presentation.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onAddHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var habitMenuTarget by remember { mutableStateOf<Habit?>(null) }

    // Long press menu
    habitMenuTarget?.let { habit ->
        HabitOptionsDialog(
            habit = habit,
            onEdit = {
                habitMenuTarget = null
                onEditHabit(habit.id)
            },
            onDelete = {
                habitMenuTarget = null
                viewModel.deleteHabit(habit.id)
            },
            onDismiss = { habitMenuTarget = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                // ── Top bar ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Avatar / greeting
                    Column {
                        Text(
                            text = "${uiState.greeting} ${uiState.currentUser?.name?.split(" ")?.firstOrNull() ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Let's build some habits",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Add habit FAB-style button
                    IconButton(
                        onClick = onAddHabit,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Indigo500, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add habit", tint = Color.White)
                    }
                }
            }

            item {
                // ── Progress summary ─────────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.habits.isNotEmpty(),
                    enter = fadeIn() + expandVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .background(DarkCard, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${uiState.completedCount} of ${uiState.totalCount}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "habits completed today",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(12.dp))
                            // Motivational message
                            val msg = when {
                                uiState.completionRate >= 1f -> "All done! You're crushing it 🎉"
                                uiState.completionRate >= 0.5f -> "More than halfway there! 💪"
                                uiState.completionRate > 0f -> "Good start, keep going! 🚀"
                                else -> "Ready to start your day? ✨"
                            }
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = Indigo400,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        ProgressRing(
                            progress = uiState.completionRate,
                            size = 88.dp,
                            strokeWidth = 8.dp,
                            color = Indigo500,
                            label = "${(uiState.completionRate * 100).toInt()}%"
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                // ── Week calendar ─────────────────────────────────────────────
                WeekCalendar(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.selectDate(it) },
                    weekDays = uiState.weekStatuses,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                // ── Date heading ──────────────────────────────────────────────
                val dateLabel = when (uiState.selectedDate) {
                    LocalDate.now() -> "Today"
                    LocalDate.now().minusDays(1) -> "Yesterday"
                    LocalDate.now().plusDays(1) -> "Tomorrow"
                    else -> uiState.selectedDate.format(
                        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
                    )
                }
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Habit list ────────────────────────────────────────────────────
            if (uiState.habits.isEmpty()) {
                item {
                    EmptyState(
                        emoji = if (uiState.totalCount == 0) "🌱" else "☀️",
                        title = if (uiState.totalCount == 0) "No habits yet" else "Free day!",
                        subtitle = if (uiState.totalCount == 0)
                            "Create your first habit to get started on your journey"
                        else
                            "No habits scheduled for this day. Enjoy the rest!",
                        actionLabel = if (uiState.totalCount == 0) "Create First Habit" else null,
                        onAction = if (uiState.totalCount == 0) onAddHabit else null,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                items(
                    items = uiState.habits,
                    key = { it.id }
                ) { habit ->
                    HabitCard(
                        habit = habit,
                        onCheckinToggle = { viewModel.toggleCheckIn(it) },
                        onNumericUpdate = { h, v -> viewModel.updateNumeric(h, v) },
                        onLongPress = { habitMenuTarget = it },
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitOptionsDialog(
    habit: Habit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Text(
                text = "${habit.icon} ${habit.name}",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            Text("What would you like to do?", color = TextSecondary)
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null, tint = Indigo400, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Edit", color = Indigo400)
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Rose400, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Delete", color = Rose400)
            }
        }
    )
}
