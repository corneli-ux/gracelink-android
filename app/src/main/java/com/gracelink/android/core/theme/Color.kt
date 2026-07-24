package com.gracelink.android.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── GraceLink brand colors ─────────────────────────────────────────────────
// Rich gold on deep dark — premium, warm, inviting for a faith community app.

// ── Backgrounds ──────────────────────────────────────────────────────────────
val Obsidian = Color(0xFF000000)       // true black (logo bg)
val Slate950 = Color(0xFF080808)       // near-black root surface
val Slate900 = Color(0xFF0F0F0F)       // primary surface
val Slate850 = Color(0xFF181818)       // elevated surface
val Slate800 = Color(0xFF1E1E1E)       // cards / containers
val Slate750 = Color(0xFF262626)       // card hover / pressed
val Slate700 = Color(0xFF333333)       // borders
val Slate600 = Color(0xFF444444)       // muted elements
val Slate500 = Color(0xFF666666)

// ── Text ──────────────────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFF5F0E6)    // warm cream (matches logo light)
val TextSecondary = Color(0xFFC4BBA8)  // muted cream
val TextMuted = Color(0xFF8A8275)      // dim warm grey
val TextSubtle = Color(0xFF5C5648)

// ── Brand gold — rich, warm, premium ──────────────────────────────────────────
val Gold200 = Color(0xFFF5E6B8)
val Gold300 = Color(0xFFE6C866)
val Gold400 = Color(0xFFD4A017)   // primary accent — rich amber-gold
val Gold500 = Color(0xFFC9911A)
val Gold600 = Color(0xFFA9760F)
val Gold700 = Color(0xFF7D5A0C)

// ── Emerald (secondary — success / prayer / growth) ────────────────────────────
val Emerald300 = Color(0xFF7BD9A8)
val Emerald400 = Color(0xFF5BC99A)
val Emerald500 = Color(0xFF3FB88A)
val Emerald600 = Color(0xFF2D9A72)
val Emerald700 = Color(0xFF1F7A5A)

// ── Sapphire (verification badge — distinct from gold) ────────────────────────
val Sapphire300 = Color(0xFF8B9FE8)
val Sapphire400 = Color(0xFF5D7FE8)
val Sapphire500 = Color(0xFF3D5AFE)
val SapphireGradient = listOf(Sapphire400, Color(0xFF7B5CF6))

// ── Semantic ───────────────────────────────────────────────────────────────────
val LiveRed = Color(0xFFFF3B3B)
val LiveRedDark = Color(0xFFDC2626)
val Rose500 = Color(0xFFF43F5E)
val Sky400 = Color(0xFF5AB8E0)
val Violet400 = Color(0xFFB89CD9)
val Violet500 = Color(0xFF9B7CC7)

// ── Glassmorphism tints ───────────────────────────────────────────────────────
val GlassLight = Color(0xFFFFFFFF).copy(alpha = 0.04f)
val GlassMedium = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val GlassStrong = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.06f)

// ── Gradients ───────────────────────────────────────────────────────────────────
val GoldGradient = listOf(Gold400, Gold600)
val EmeraldGradient = listOf(Emerald400, Emerald600)
val SunsetGradient = listOf(Gold400, Rose500)
val DuskGradient = listOf(Violet500, Sky400)
val EmberGradient = listOf(Color(0xFF3A2A1A), Color(0xFF111111))
val PrayerGradient = listOf(Color(0xFF1A3A2A), Color(0xFF111111))
val HeroGradient = listOf(Slate900, Slate800)
val DeepScrimGradient = listOf(
    Color(0x00000000),
    Color(0x99000000),
    Color(0xF0000000),
)
val CardGlowGradient = listOf(
    Gold400.copy(alpha = 0.08f),
    Color.Transparent,
)
