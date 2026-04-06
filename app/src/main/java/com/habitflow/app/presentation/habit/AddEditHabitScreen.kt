package com.habitflow.app.presentation.habit

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.domain.model.HabitType
import com.habitflow.app.presentation.components.parseColor
import com.habitflow.app.presentation.theme.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

private val habitIcons = listOf(
    "🏃", "💧", "📚", "🧘", "💊", "🍎", "😴", "💪", "🎯", "✍️",
    "🎵", "🧹", "🌿", "☀️", "🚴", "🏋️", "🧠", "❤️", "🛑", "✅"
)

private val habitColorHexes = listOf(
    "#EF4444", "#F97316", "#EAB308", "#22C55E", "#14B8A6",
    "#3B82F6", "#6366F1", "#A855F7", "#EC4899", "#64748B"
)

@Composable
fun AddEditHabitScreen(
    habitId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditHabitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(habitId) {
        if (!habitId.isNullOrBlank()) {
            viewModel.loadHabit(habitId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary)
                }
                Text(
                    text = if (state.isEditMode) "Edit Habit" else "New Habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
                Button(
                    onClick = { viewModel.save { onDone() } },
                    enabled = state.isValid && !state.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save", color = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Habit name ─────────────────────────────────────────────
                SectionLabel("Habit Name")
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    placeholder = { Text("e.g. Morning run", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo500,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Indigo400,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = TextPrimary)
                )

                Spacer(Modifier.height(24.dp))

                // ── Habit type ─────────────────────────────────────────────
                SectionLabel("Habit Type")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitType.values().forEach { type ->
                        val selected = state.type == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.updateType(type) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) Indigo500 else DarkCard,
                            border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    if (type == HabitType.CHECK_IN) "✓" else "123",
                                    fontSize = 22.sp,
                                    color = if (selected) Color.White else TextSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    type.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else TextSecondary,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    type.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White.copy(0.7f) else TextTertiary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // ── Numeric goal (only if NUMERIC) ─────────────────────────
                AnimatedVisibility(
                    visible = state.type == HabitType.NUMERIC,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(Modifier.height(24.dp))
                        SectionLabel("Goal (optional)")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.goalValue,
                                onValueChange = { viewModel.updateGoalValue(it) },
                                placeholder = { Text("e.g. 8", color = TextTertiary) },
                                label = { Text("Target") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.unit,
                                onValueChange = { viewModel.updateUnit(it) },
                                placeholder = { Text("glasses", color = TextTertiary) },
                                label = { Text("Unit") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Frequency ──────────────────────────────────────────────
                SectionLabel("Schedule")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayOfWeek.values().forEach { day ->
                        val selected = day in state.frequency
                        val dayLabel = day.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) Indigo500 else DarkCard
                                )
                                .border(
                                    1.dp,
                                    if (selected) Indigo500 else DarkBorder,
                                    CircleShape
                                )
                                .clickable { viewModel.toggleDay(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White else TextSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Icon picker ────────────────────────────────────────────
                SectionLabel("Icon")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(habitIcons) { icon ->
                        val selected = state.selectedIcon == icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) parseColor(state.selectedColor).copy(0.3f)
                                    else DarkCard
                                )
                                .border(
                                    if (selected) 2.dp else 0.dp,
                                    if (selected) parseColor(state.selectedColor) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.updateIcon(icon) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icon, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Color picker ───────────────────────────────────────────
                SectionLabel("Color")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(habitColorHexes) { hex ->
                        val selected = state.selectedColor == hex
                        val color = parseColor(hex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (selected) 3.dp else 0.dp,
                                    Color.White.copy(if (selected) 0.9f else 0f),
                                    CircleShape
                                )
                                .clickable { viewModel.updateColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Error
                state.error?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, color = Rose400, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Indigo500,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = Indigo400,
    unfocusedLabelColor = TextTertiary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Indigo400,
    focusedContainerColor = DarkCard,
    unfocusedContainerColor = DarkCard
)
