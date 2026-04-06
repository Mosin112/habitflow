package com.habitflow.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.presentation.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

data class DayStatus(
    val date: LocalDate,
    val completionRate: Float // 0f = nothing, 0.01..0.99 = partial, 1f = all done
)

@Composable
fun WeekCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    weekDays: List<DayStatus> = emptyList(),
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()

    // Build the current week (Mon–Sun)
    val weekStart = selectedDate.minusDays(
        (selectedDate.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong().coerceAtLeast(0L)
    )
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }
    val statusMap = weekDays.associateBy { it.date }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == today
            val isFuture = date.isAfter(today)
            val status = statusMap[date]

            val bgColor by animateColorAsState(
                targetValue = when {
                    isSelected -> Indigo500
                    isToday -> Indigo800.copy(alpha = 0.4f)
                    else -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "day_bg"
            )
            val textColor = when {
                isSelected -> Color.White
                isToday -> Indigo400
                isFuture -> TextDisabled
                else -> TextSecondary
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onDateSelected(date) }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                // Day label (M, T, W…)
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
                Spacer(Modifier.height(4.dp))
                // Date number
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(5.dp))
                // Completion dot
                val dotColor = when {
                    isFuture || status == null -> Color.Transparent
                    status.completionRate >= 1f -> Emerald400
                    status.completionRate > 0f -> Amber400
                    else -> Rose400.copy(alpha = 0.6f)
                }
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}
