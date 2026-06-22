package com.gracelink.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material 3 color scheme.
 *
 * The spec mandates a dark theme as the default for media apps.
 * We *do* expose a light scheme for completeness (e.g. user override), but
 * [GraceLinkTheme] will use dark by default.
 */
private val DarkColors = darkColorScheme(
    primary            = Gold500,
    onPrimary          = Color(0xFF1A1206),
    primaryContainer   = Gold600,
    onPrimaryContainer = Gold300,
    secondary          = Emerald500,
    onSecondary        = Color(0xFF001E13),
    secondaryContainer = Emerald600,
    onSecondaryContainer = Emerald400,
    tertiary           = Sky400,
    onTertiary         = Color(0xFF001F2A),
    background         = Slate950,
    onBackground       = TextPrimary,
    surface            = Slate900,
    onSurface          = TextPrimary,
    surfaceVariant     = Slate800,
    onSurfaceVariant   = TextSecondary,
    surfaceTint        = Gold500,
    outline            = Slate600,
    outlineVariant     = Color(0xFF334155),
    error              = Rose500,
    onError            = Color(0xFFFFFFFF),
    errorContainer     = Color(0xFF93000A),
    onErrorContainer   = Color(0xFFFFDAD6),
    inverseSurface     = Color(0xFFE2E8F0),
    inverseOnSurface   = Color(0xFF1E293B),
    inversePrimary     = Gold600,
    scrim              = Color(0xFF000000),
)

private val LightColors = lightColorScheme(
    primary            = Gold600,
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Gold300,
    onPrimaryContainer = Color(0xFF422B00),
    secondary          = Emerald600,
    onSecondary        = Color(0xFFFFFFFF),
    background         = Color(0xFFF8FAFC),
    onBackground       = Color(0xFF0F172A),
    surface            = Color(0xFFFFFFFF),
    onSurface          = Color(0xFF0F172A),
    surfaceVariant     = Color(0xFFE2E8F0),
    onSurfaceVariant   = Color(0xFF475569),
    outline            = Color(0xFFCBD5E1),
    error              = Rose500,
    onError            = Color(0xFFFFFFFF),
)

/**
 * App-wide theme wrapper. Always uses the dark scheme (per spec) unless
 * [forceLight] is explicitly requested by a future user preference.
 */
@Composable
fun GraceLinkTheme(
    darkTheme: Boolean = true,  // spec: dark preferred
    forceLight: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        forceLight -> LightColors
        darkTheme -> DarkColors
        isSystemInDarkTheme() -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GraceTypography,
        shapes = GraceShapes,
        content = content
    )
}
