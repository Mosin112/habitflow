package com.habitflow.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitType
import com.habitflow.app.presentation.theme.*

@Composable
fun HabitCard(
    habit: Habit,
    onCheckinToggle: (Habit) -> Unit,
    onNumericUpdate: (Habit, Float) -> Unit,
    onLongPress: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val habitColor = parseColor(habit.colorHex)
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(habit.id) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = { onLongPress(habit) }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = DarkCard,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(habitColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(habit.icon, fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                // Name + frequency
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = habit.frequency.size.let { days ->
                            when (days) {
                                7 -> "Every day"
                                5 -> "Weekdays"
                                2 -> "Weekends"
                                else -> "$days days/week"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Streak badge
                if (habit.currentStreak > 0) {
                    StreakBadge(streak = habit.currentStreak, compact = true)
                    Spacer(Modifier.width(8.dp))
                }

                // Action area
                when (habit.type) {
                    HabitType.CHECK_IN -> CheckInButton(
                        isCompleted = habit.isCompletedToday,
                        color = habitColor,
                        onClick = { onCheckinToggle(habit) }
                    )
                    HabitType.NUMERIC -> NumericStepper(
                        habit = habit,
                        color = habitColor,
                        onUpdate = { value -> onNumericUpdate(habit, value) }
                    )
                }
            }

            // Progress bar for numeric habits with a goal
            if (habit.type == HabitType.NUMERIC && habit.goalValue != null && habit.goalValue > 0) {
                Spacer(Modifier.height(10.dp))
                NumericProgressBar(
                    habit = habit,
                    color = habitColor
                )
            }
        }
    }
}

@Composable
private fun CheckInButton(
    isCompleted: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "check_scale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(200),
        label = "check_bg"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .background(
                color = if (isCompleted) color else color.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = isCompleted,
                transitionSpec = {
                    scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy)) +
                    fadeIn() togetherWith scaleOut() + fadeOut()
                },
                label = "check_icon"
            ) { completed ->
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (completed) "Mark incomplete" else "Mark complete",
                    tint = if (completed) Color.White else color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun NumericStepper(
    habit: Habit,
    color: Color,
    onUpdate: (Float) -> Unit
) {
    val currentValue = habit.todayNumericValue ?: 0f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Decrement
        IconButton(
            onClick = { if (currentValue > 0) onUpdate((currentValue - 1).coerceAtLeast(0f)) },
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Default.Remove, null, tint = color, modifier = Modifier.size(16.dp))
        }

        // Value display
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
            Text(
                text = if (currentValue == currentValue.toLong().toFloat())
                    currentValue.toLong().toString()
                else "%.1f".format(currentValue),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (currentValue > 0) color else TextSecondary
            )
            habit.unit?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)
            }
        }

        // Increment
        IconButton(
            onClick = { onUpdate(currentValue + 1) },
            modifier = Modifier
                .size(32.dp)
                .background(color, CircleShape)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun NumericProgressBar(habit: Habit, color: Color) {
    val progress = habit.progressToGoal
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "num_progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(habit.todayNumericValue ?: 0f).let {
                    if (it == it.toLong().toFloat()) it.toLong().toString() else "%.1f".format(it)
                }} ${habit.unit ?: ""}",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = "Goal: ${habit.goalValue?.toLong()} ${habit.unit ?: ""}",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        com.habitflow.app.presentation.theme.Indigo500
    }
}
