package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmergencyRuby,
    onPrimary = Color.White,
    secondary = SoftAqua,
    onSecondary = Color.White,
    tertiary = WarnAmber,
    onTertiary = MidnightBase,
    background = MidnightBase,
    onBackground = SoftWhite,
    surface = DepthSurface,
    onSurface = SoftWhite,
    surfaceVariant = CardSurface,
    onSurfaceVariant = SoftWhite,
    outline = OutlineSlate,
    error = EmergencyRuby,
    onError = Color.White
)

// Since safety should be high-contrast and consistent, we enforce a unified 
// professional dark theme that keeps users alert while preserving night vision.
private val LightColorScheme = darkColorScheme( // Enforce eye-friendly theme
    primary = EmergencyRuby,
    onPrimary = Color.White,
    secondary = SoftAqua,
    onSecondary = Color.White,
    tertiary = WarnAmber,
    onTertiary = MidnightBase,
    background = MidnightBase,
    onBackground = SoftWhite,
    surface = DepthSurface,
    onSurface = SoftWhite,
    surfaceVariant = CardSurface,
    onSurfaceVariant = SoftWhite,
    outline = OutlineSlate,
    error = EmergencyRuby,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep safety branding consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
