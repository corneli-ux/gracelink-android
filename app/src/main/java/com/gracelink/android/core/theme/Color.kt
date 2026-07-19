package com.gracelink.android.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── GraceLink logo colors ──────────────────────────────────────────────────
// Logo: warm gold/cream (#D1BE9B) on pure black (#030303)
// Theme follows the logo: black bg, warm gold accents, cream text

val Obsidian = Color(0xFF000000)       // pure black (matches logo bg)
val Slate950 = Color(0xFF0A0A0A)       // near-black surface
val Slate900 = Color(0xFF111111)       // root surface
val Slate850 = Color(0xFF1A1A1A)       // elevated surface
val Slate800 = Color(0xFF222222)       // cards
val Slate750 = Color(0xFF2A2A2A)       // card hover/pressed
val Slate700 = Color(0xFF333333)       // borders
val Slate600 = Color(0xFF444444)       // muted
val Slate500 = Color(0xFF666666)

// ── Text ────────────────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFF5F0E6)    // warm cream (matches logo light)
val TextSecondary = Color(0xFFC4BBA8)  // muted cream
val TextMuted = Color(0xFF8A8275)      // dim warm grey
val TextSubtle = Color(0xFF5C5648)

// ── Brand gold (warm, matches logo #D1BE9B) ─────────────────────────────────
val Gold200 = Color(0xFFF0E6D2)
val Gold300 = Color(0xFFE5D5B8)
val Gold400 = Color(0xFFD1BE9B)   // exact logo gold
val Gold500 = Color(0xFFD1BE9B)   // primary accent = logo color
val Gold600 = Color(0xFFB8A585)
val Gold700 = Color(0xFF968868)

// ── Emerald (secondary accent for success/prayer) ───────────────────────────
val Emerald300 = Color(0xFF7BD9A8)
val Emerald400 = Color(0xFF5BC99A)
val Emerald500 = Color(0xFF3FB88A)   // slightly muted emerald
val Emerald600 = Color(0xFF2D9A72)
val Emerald700 = Color(0xFF1F7A5A)

// ── Semantic ────────────────────────────────────────────────────────────────
val LiveRed = Color(0xFFFF3B3B)
val LiveRedDark = Color(0xFFDC2626)
val Rose500 = Color(0xFFF43F5E)
val Sky400 = Color(0xFF5AB8E0)
val Violet400 = Color(0xFFB89CD9)
val Violet500 = Color(0xFF9B7CC7)

// ── Glassmorphism tints ─────────────────────────────────────────────────────
val GlassLight = Color(0xFFFFFFFF).copy(alpha = 0.06f)
val GlassMedium = Color(0xFFFFFFFF).copy(alpha = 0.10f)
val GlassStrong = Color(0xFFFFFFFF).copy(alpha = 0.14f)
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.08f)

// ── Gradients ───────────────────────────────────────────────────────────────
val GoldGradient = listOf(Gold400, Gold600)
val EmeraldGradient = listOf(Emerald400, Emerald600)
val SunsetGradient = listOf(Gold400, Rose500)
val DuskGradient = listOf(Violet500, Sky400)
val EmberGradient = listOf(Color(0xFF3A2A1A), Color(0xFF111111))
val PrayerGradient = listOf(Color(0xFF1A3A2A), Color(0xFF111111))
val DeepScrimGradient = listOf(
    Color(0x00000000),
    Color(0x99000000),
    Color(0xF0000000),
)
