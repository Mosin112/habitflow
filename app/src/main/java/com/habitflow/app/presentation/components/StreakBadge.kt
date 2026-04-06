package com.habitflow.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.presentation.theme.Amber400
import com.habitflow.app.presentation.theme.Amber500
import com.habitflow.app.presentation.theme.DarkCard

@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (streak <= 0) return

    val bgColor = Amber500.copy(alpha = 0.15f)
    val textColor = Amber400

    AnimatedVisibility(
        visible = streak > 0,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(20.dp))
                .padding(
                    horizontal = if (compact) 8.dp else 10.dp,
                    vertical = if (compact) 3.dp else 5.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "🔥",
                fontSize = if (compact) 11.sp else 13.sp
            )
            Text(
                text = streak.toString(),
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 11.sp else 13.sp
                )
            )
        }
    }
}
