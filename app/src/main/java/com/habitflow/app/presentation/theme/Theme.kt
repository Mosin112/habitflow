package com.habitflow.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo800,
    onPrimaryContainer = Indigo100,
    secondary = Violet500,
    onSecondary = Color.White,
    secondaryContainer = Violet600,
    onSecondaryContainer = Violet300,
    tertiary = Amber500,
    onTertiary = Color.White,
    tertiaryContainer = Amber600,
    onTertiaryContainer = Amber300,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = Rose500,
    onError = Color.White,
    scrim = Color(0x99000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo900,
    secondary = Violet600,
    onSecondary = Color.White,
    secondaryContainer = Violet300,
    onSecondaryContainer = Violet600,
    tertiary = Amber500,
    onTertiary = Color.White,
    tertiaryContainer = Amber300,
    onTertiaryContainer = Amber600,
    background = LightBg,
    onBackground = Color(0xFF1A1A2E),
    surface = LightSurface,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = LightCard,
    onSurfaceVariant = Color(0xFF4B4B5E),
    outline = LightBorder,
    error = Rose500,
    onError = Color.White
)

@Composable
fun HabitFlowTheme(
    darkTheme: Boolean = true, // Default to dark — feels more premium
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HabitFlowTypography,
        content = content
    )
}
