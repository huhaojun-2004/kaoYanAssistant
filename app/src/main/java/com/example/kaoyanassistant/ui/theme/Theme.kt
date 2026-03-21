package com.example.kaoyanassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Pine80,
    secondary = Slate80,
    tertiary = Apricot80,
    background = Ink,
    surface = PineDark,
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = Cloud,
    onSurface = Cloud
)

private val LightColorScheme = lightColorScheme(
    primary = Pine,
    secondary = Slate,
    tertiary = Apricot,
    background = Cream,
    surface = Paper,
    surfaceVariant = Cloud,
    onPrimary = Paper,
    onSecondary = Paper,
    onTertiary = Ink,
    onBackground = Ink,
    onSurface = Ink
)

@Composable
fun KaoYanAssistantTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
