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

// ── Brand gold (enriched per request -- was a pale muted cream D1BE9B,
// now a deeper, more saturated gold. This maps directly to
// MaterialTheme.colorScheme.primary in Theme.kt, so every button, badge,
// and accent built against theme colors picks this up automatically --
// no per-screen changes needed) ─────────────────────────────────────────
val Gold200 = Color(0xFFF2DFA0)
val Gold300 = Color(0xFFE6C866)
val Gold400 = Color(0xFFD4A017)   // rich amber-gold, primary accent
val Gold500 = Color(0xFFC9911A)
val Gold600 = Color(0xFFA9760F)
val Gold700 = Color(0xFF7D5A0C)

// ── Emerald (secondary accent for success/prayer) ───────────────────────────
val Emerald300 = Color(0xFF7BD9A8)
val Emerald400 = Color(0xFF5BC99A)
val Emerald500 = Color(0xFF3FB88A)   // slightly muted emerald
val Emerald600 = Color(0xFF2D9A72)
val Emerald700 = Color(0xFF1F7A5A)

// ── Platinum (verification badge, distinct from the warm brand gold) ───────
// Rich sapphire-to-violet -- reads as premium/verified without competing
// with the app's gold accent used everywhere else. First applied to church
// verification checkmarks; previewed on Splash's glow to see how a
// platinum-toned accent would feel elsewhere before any wider rollout.
val PlatinumBlue = Color(0xFF3D5AFE)
val PlatinumViolet = Color(0xFF8B5CF6)
val PlatinumGradient = listOf(PlatinumBlue, PlatinumViolet)

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
