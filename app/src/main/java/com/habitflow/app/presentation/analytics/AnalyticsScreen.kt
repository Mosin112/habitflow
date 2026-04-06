package com.habitflow.app.presentation.analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.domain.model.AnalyticsData
import com.habitflow.app.domain.model.HabitStat
import com.habitflow.app.presentation.components.ProgressRing
import com.habitflow.app.presentation.components.StreakBadge
import com.habitflow.app.presentation.theme.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val analytics by viewModel.analytics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(Modifier.statusBarsPadding())
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp)
                )
                Text(
                    "Your habit performance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 20.dp, bottom = 20.dp)
                )
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Indigo500)
                    }
                }
            } else if (analytics != null) {
                val data = analytics!!

                item {
                    // ── Today summary ─────────────────────────────────────
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
                            Text("Today", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            StatItem("✅", "${data.completedToday}", "completed")
                            Spacer(Modifier.height(4.dp))
                            StatItem("📊", "${data.totalHabits}", "total habits")
                            Spacer(Modifier.height(4.dp))
                            StatItem("🔥", "${data.longestStreak}", "longest streak")
                        }
                        ProgressRing(
                            progress = data.todayCompletionRate,
                            size = 96.dp,
                            strokeWidth = 9.dp,
                            color = Emerald500,
                            label = "${(data.todayCompletionRate * 100).toInt()}%",
                            sublabel = "today"
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    // ── Weekly bar chart ──────────────────────────────────
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        color = DarkCard,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "This Week",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(16.dp))
                            WeekBarChart(rates = data.weeklyCompletionRates)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (data.habitStats.isNotEmpty()) {
                    item {
                        Text(
                            "Habit Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(data.habitStats, key = { it.habit.id }) { stat ->
                        HabitStatCard(stat = stat, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(emoji: String, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun WeekBarChart(rates: List<Float>) {
    val days = DayOfWeek.values()
    val maxRate = rates.maxOrNull()?.coerceAtLeast(0.01f) ?: 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        rates.forEachIndexed { index, rate ->
            val day = days.getOrNull(index)
            val animatedHeight by animateFloatAsState(
                targetValue = rate / maxRate,
                animationSpec = tween(800, easing = FastOutSlowInEasing, delayMillis = index * 60),
                label = "bar_$index"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.6f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val barColor = when {
                        rate >= 1f -> Emerald500
                        rate > 0f -> Indigo500
                        else -> DarkBorder
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedHeight.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(barColor)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = day?.getDisplayName(TextStyle.NARROW, Locale.getDefault()) ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun HabitStatCard(stat: HabitStat, modifier: Modifier = Modifier) {
    val habitColor = com.habitflow.app.presentation.components.parseColor(stat.habit.colorHex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkCard,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(habitColor.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stat.habit.icon, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stat.habit.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        "${(stat.completionRate30Days * 100).toInt()}% last 30 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                StreakBadge(streak = stat.currentStreak, compact = false)
            }

            Spacer(Modifier.height(10.dp))

            // Completion bar
            LinearProgressIndicator(
                progress = { stat.completionRate30Days.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = habitColor,
                trackColor = habitColor.copy(0.15f)
            )

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${stat.totalCompletions} total completions",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Text(
                    "Best: ${stat.bestStreak} days",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}
