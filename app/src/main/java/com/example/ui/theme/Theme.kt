package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color.Black,
    primaryContainer = JarvisSurfaceElevated,
    onPrimaryContainer = JarvisCyan,
    secondary = JarvisBlue,
    onSecondary = Color.White,
    secondaryContainer = JarvisSurfaceBorder,
    onSecondaryContainer = JarvisTextPrimary,
    tertiary = JarvisGold,
    onTertiary = Color.Black,
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceElevated,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisSurfaceBorder,
    error = JarvisRed,
    onError = Color.White
)

private val JarvisLightColorScheme = lightColorScheme(
    primary = JarvisLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = JarvisCyanDark,
    onSecondary = Color.White,
    background = JarvisLightBackground,
    onBackground = JarvisLightText,
    surface = JarvisLightSurface,
    onSurface = JarvisLightText,
    surfaceVariant = Color(0xFFE2E8F3),
    onSurfaceVariant = Color(0xFF444F60),
    outline = Color(0xFFBDC7D6),
    error = JarvisRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to futuristic Dark HUD for JARVIS
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JarvisDarkColorScheme else JarvisLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
