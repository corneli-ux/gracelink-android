package com.gracelink.android.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for GraceLink.
 *
 * Uses the new Navigation Compose type-safe routes (Kotlinx Serialization).
 * Each object is a route — no stringly-typed paths.
 */
sealed interface GraceDestination : java.io.Serializable {

    @Serializable data object Splash      : GraceDestination
    @Serializable data object Onboarding  : GraceDestination
    @Serializable data object Home        : GraceDestination
    @Serializable data object Library     : GraceDestination
    @Serializable data object Events      : GraceDestination
    @Serializable data object Prayer      : GraceDestination
    @Serializable data object Profile     : GraceDestination

    @Serializable data class Player(val contentId: String) : GraceDestination
    @Serializable data class LiveSession(val sessionId: String) : GraceDestination
}

/**
 * Bottom-nav destinations — used by the main scaffold.
 */
val bottomNavDestinations = listOf(
    GraceDestination.Home,
    GraceDestination.Library,
    GraceDestination.Events,
    GraceDestination.Prayer,
    GraceDestination.Profile,
)
