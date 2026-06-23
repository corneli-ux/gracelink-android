package com.gracelink.android.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary            = Gold500,
    onPrimary          = Color(0xFF1A0F00),
    primaryContainer   = Gold700,
    onPrimaryContainer = Gold200,
    secondary          = Emerald500,
    onSecondary        = Color(0xFF00211A),
    secondaryContainer = Emerald700,
    onSecondaryContainer = Emerald300,
    tertiary           = Violet400,
    onTertiary         = Color(0xFF1E0F3A),
    background         = Obsidian,
    onBackground       = TextPrimary,
    surface            = Slate950,
    onSurface          = TextPrimary,
    surfaceVariant     = Slate800,
    onSurfaceVariant   = TextSecondary,
    surfaceTint        = Gold500,
    outline            = Slate700,
    outlineVariant     = Color(0xFF233044),
    error              = Rose500,
    onError            = Color(0xFFFFFFFF),
    errorContainer     = Color(0xFF93000A),
    onErrorContainer   = Color(0xFFFFDAD6),
    inverseSurface     = Color(0xFFE2E8F0),
    inverseOnSurface   = Color(0xFF1E293B),
    inversePrimary     = Gold600,
    scrim              = Color(0xFF000000),
)

@Composable
fun GraceLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = GraceTypography,
        shapes = GraceShapes,
        content = content
    )
}
