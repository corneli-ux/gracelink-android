package com.gracelink.android.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * GraceLink color tokens.
 *
 * Source of truth: §4 "UI/UX Design System & Guidelines" of the Android spec.
 *  Background   #0F172A   deep slate
 *  Surfaces     #1E293B   elevated cards
 *  Accent Gold  #F59E0B   CTAs, LIVE indicator, highlights
 *  Success      #10B981   "I prayed", completed actions
 *  Text Primary #F8FAFC
 *  Text Subtle  #94A3B8
 *
 * Dark theme is the default — the spec says "dark theme preferred for media apps".
 */

// ── Base palette ─────────────────────────────────────────────────────────────
val Slate950 = Color(0xFF0F172A)   // app background
val Slate900 = Color(0xFF111C33)   // root surface
val Slate800 = Color(0xFF1E293B)   // cards
val Slate700 = Color(0xFF27364A)   // pressed / selected card
val Slate600 = Color(0xFF334155)   // borders / dividers
val Slate500 = Color(0xFF475569)

val TextPrimary   = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted     = Color(0xFF64748B)

// ── Brand / accent ───────────────────────────────────────────────────────────
val Gold400  = Color(0xFFFBBF24)
val Gold500  = Color(0xFFF59E0B)   // primary CTA
val Gold600  = Color(0xFFD97706)
val Gold300  = Color(0xFFFCD34D)

val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981)  // success / "I prayed"
val Emerald600 = Color(0xFF059669)

val Rose500  = Color(0xFFF43F5E)   // destructive / report
val Sky400   = Color(0xFF38BDF8)   // info / links
val Violet400 = Color(0xFFA78BFA)

// ── LIVE indicator pulsing red ───────────────────────────────────────────────
val LiveRed = Color(0xFFFF3B3B)

// ── Gradients (used on hero banners + player background) ─────────────────────
val HeroGradient = listOf(
    Color(0xFF1E293B),
    Color(0xFF0F172A),
)

val GoldGradient = listOf(
    Gold400,
    Gold600,
)

val PlayerScrim = listOf(
    Color(0xCC0F172A),   // 80% slate
    Color(0x000F172A),
)

val LiveCardGradient = listOf(
    Color(0xFF7C2D12),   // warm ember
    Color(0xFF1E293B),
)

val PrayerCardGradient = listOf(
    Color(0xFF064E3B),   // deep emerald
    Color(0xFF1E293B),
)

/**
 * Reusable brush factories — keep gradients centralized so the look stays
 * consistent across screens.
 */
object GraceGradients {
    fun hero() = Brush.verticalGradient(HeroGradient)
    fun gold() = Brush.horizontalGradient(GoldGradient)
    fun playerScrim() = Brush.verticalGradient(PlayerScrim)
    fun liveCard() = Brush.diagonalGradient(LiveCardGradient)
    fun prayerCard() = Brush.diagonalGradient(PrayerCardGradient)
}
