package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    primaryContainer = NeonPurpleDark,
    onPrimaryContainer = NeonPurpleLight,
    secondary = NeonCyan,
    onSecondary = GraphiteBlack,
    secondaryContainer = NeonCyanDark,
    onSecondaryContainer = NeonCyanLight,
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = GraphiteBlack,
    onBackground = TextPrimary,
    surface = GraphiteDark,
    onSurface = TextPrimary,
    surfaceVariant = GraphiteSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GraphiteBorder,
    outlineVariant = GraphiteSurface
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPurpleDark,
    onPrimary = Color.White,
    primaryContainer = NeonPurpleLight,
    onPrimaryContainer = Color.Black,
    secondary = NeonCyanDark,
    onSecondary = Color.White,
    secondaryContainer = NeonCyanLight,
    onSecondaryContainer = Color.Black,
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder
)

@Composable
fun SeraAkisosTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
