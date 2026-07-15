package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CelestialColorScheme = darkColorScheme(
    primary = CelestialPrimary,
    onPrimary = CelestialOnPrimary,
    secondary = CelestialSecondary,
    onSecondary = CelestialOnSecondary,
    background = CelestialBackground,
    onBackground = CelestialText,
    surface = CelestialSurface,
    onSurface = CelestialText
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CelestialColorScheme,
        typography = Typography,
        content = content
    )
}
