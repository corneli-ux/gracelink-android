package com.gracelink.android.core.theme

import androidx.compose.ui.graphics.Color

// ── Core slate scale (deep, layered surfaces) ───────────────────────────────
val Obsidian = Color(0xFF080B14)       // deepest background
val Slate950 = Color(0xFF0F172A)       // primary background
val Slate900 = Color(0xFF141C2E)       // root surface
val Slate850 = Color(0xFF1A2338)       // elevated surface
val Slate800 = Color(0xFF1E293B)       // cards
val Slate750 = Color(0xFF27344A)       // card hover/pressed
val Slate700 = Color(0xFF334155)       // borders
val Slate600 = Color(0xFF475569)       // muted text
val Slate500 = Color(0xFF64748B)

// ── Text ────────────────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFFCBD5E1)
val TextMuted = Color(0xFF94A3B8)
val TextSubtle = Color(0xFF64748B)

// ── Brand gold (warm, hopeful) ──────────────────────────────────────────────
val Gold200 = Color(0xFFFDE68A)
val Gold300 = Color(0xFFFCD34D)
val Gold400 = Color(0xFFFBBF24)
val Gold500 = Color(0xFFF59E0B)   // primary CTA
val Gold600 = Color(0xFFD97706)
val Gold700 = Color(0xFFB45309)

// ── Emerald (success, growth, "I prayed") ───────────────────────────────────
val Emerald300 = Color(0xFF6EE7B7)
val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)
val Emerald700 = Color(0xFF047857)

// ── Semantic ────────────────────────────────────────────────────────────────
val LiveRed = Color(0xFFFF3B3B)
val LiveRedDark = Color(0xFFDC2626)
val Rose500 = Color(0xFFF43F5E)
val Sky400 = Color(0xFF38BDF8)
val Violet400 = Color(0xFFA78BFA)
val Violet500 = Color(0xFF8B5CF6)

// ── Glassmorphism tints (for blurred overlays) ──────────────────────────────
val GlassLight = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val GlassMedium = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val GlassStrong = Color(0xFFFFFFFF).copy(alpha = 0.16f)
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.10f)

// ── Gradients ───────────────────────────────────────────────────────────────
val GoldGradient = listOf(Gold400, Gold600)
val EmeraldGradient = listOf(Emerald400, Emerald600)
val SunsetGradient = listOf(Gold500, Rose500)
val DuskGradient = listOf(Violet500, Sky400)
val EmberGradient = listOf(Color(0xFF7C2D12), Color(0xFF1E293B))
val PrayerGradient = listOf(Color(0xFF064E3B), Color(0xFF1E293B))
val DeepScrimGradient = listOf(
    Color(0x000F172A),
    Color(0x990F172A),
    Color(0xF00F172A),
)
