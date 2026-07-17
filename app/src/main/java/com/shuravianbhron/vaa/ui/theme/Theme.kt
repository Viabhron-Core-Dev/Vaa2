package com.shuravianbhron.vaa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CelestialDarkColorScheme = darkColorScheme(
    primary = CelestialPrimary,
    onPrimary = CelestialOnPrimary,
    secondary = CelestialSecondary,
    onSecondary = CelestialOnSecondary,
    background = CelestialBackground,
    onBackground = CelestialText,
    surface = CelestialSurface,
    onSurface = CelestialText
)

private val CelestialLightColorScheme = lightColorScheme(
    primary = CelestialPrimary,
    onPrimary = CelestialLightOnPrimary,
    secondary = CelestialLightSecondary,
    onSecondary = CelestialLightOnSecondary,
    background = CelestialLightBackground,
    onBackground = CelestialLightText,
    surface = CelestialLightSurface,
    onSurface = CelestialLightText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Set to false to force light mode
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        CelestialDarkColorScheme
    } else {
        CelestialLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
