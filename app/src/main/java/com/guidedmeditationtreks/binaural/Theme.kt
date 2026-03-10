package com.guidedmeditationtreks.binaural

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFB794F6),
        onPrimary = Color(0xFF1E1B20),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        onSurface = Color(0xFFE6E1E5),
        onSurfaceVariant = Color(0xFFE0E0E0),
        outline = Color(0xFF9B8FA3),
        outlineVariant = Color(0xFF7A6E82),
    )

private val LightColorScheme = lightColorScheme()

@Composable
fun BinauralTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
