package com.example.junglenav.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Tide,
    onPrimary = NightForest,
    secondary = Ember,
    onSecondary = NightForest,
    tertiary = Moss,
    background = NightForest,
    onBackground = Moon,
    surface = Dune,
    onSurface = Moon,
    surfaceVariant = LagoonDeep,
    onSurfaceVariant = Moon,
    outline = Tide.copy(alpha = 0.4f),
    error = Ember,
    onError = NightForest,
)

private val LightColorScheme = lightColorScheme(
    primary = Lagoon,
    onPrimary = Foam,
    secondary = Coral,
    onSecondary = Foam,
    tertiary = Amber,
    onTertiary = Slate,
    background = Sand,
    onBackground = Slate,
    surface = Foam,
    onSurface = Slate,
    surfaceVariant = Mist,
    onSurfaceVariant = LagoonDeep,
    outline = Lagoon.copy(alpha = 0.18f),
    error = Clay,
    onError = Foam,
)

private val LowLightColorScheme = lightColorScheme(
    primary = LagoonDeep,
    onPrimary = Foam,
    secondary = Fern,
    onSecondary = Foam,
    tertiary = Amber,
    onTertiary = Slate,
    background = Cloud,
    onBackground = Slate,
    surface = Sand,
    onSurface = Slate,
    surfaceVariant = Mist.copy(alpha = 0.82f),
    onSurfaceVariant = LagoonDeep,
    outline = Fern.copy(alpha = 0.22f),
    error = Clay,
    onError = Foam,
)

@Composable
fun JungleNavTheme(
    darkTheme: Boolean = false,
    lowLightModeEnabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        lowLightModeEnabled -> LowLightColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
