package com.foldcut.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FoldCutDark = darkColorScheme(
    primary = Color(0xFF9BC7FF),
    onPrimary = Color(0xFF00315A),
    primaryContainer = Color(0xFF164A79),
    onPrimaryContainer = Color(0xFFD2E7FF),
    secondary = Color(0xFFC5C5F5),
    onSecondary = Color(0xFF2E2E4D),
    secondaryContainer = Color(0xFF464668),
    onSecondaryContainer = Color(0xFFE2E1FF),
    background = Color(0xFF101116),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF101116),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC6C6D0),
    outline = Color(0xFF8F9099),
    error = Color(0xFFFFB4AB)
)

private val FoldCutLight = lightColorScheme(
    primary = Color(0xFF175A96),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E7FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF5A5A81),
    background = Color(0xFFF9F9FF),
    surface = Color(0xFFF9F9FF)
)

@Composable
fun FoldCutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) FoldCutDark else FoldCutLight,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
