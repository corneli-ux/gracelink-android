package com.gracelink.android.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for GraceLink.
 *
 * Clean flow:
 *   Splash -> (first launch only) Onboarding -> Home
 *   Splash -> (returning user) Home directly
 *
 * Home is reachable and fully usable as a GUEST -- no forced login before or
 * after onboarding. Auth is a destination you can navigate *to* from Profile
 * or from a specific gated action (posting a prayer, requesting to join a
 * church, opening the Church Portal), never a mandatory gate in the graph.
 */
@Serializable
sealed interface GraceRoute {

    // Pre-auth / first-run
    @Serializable data object Splash : GraceRoute
    @Serializable data object Onboarding : GraceRoute
    @Serializable data object Auth : GraceRoute
    @Serializable data object Registration : GraceRoute

    // Single unified hub -- replaces the old forced PortalHub step
    @Serializable data object Home : GraceRoute

    // Core destinations (bottom nav)
    @Serializable data object Podcasts : GraceRoute
    @Serializable data object LiveSpaces : GraceRoute
    @Serializable data object Community : GraceRoute
    @Serializable data object Profile : GraceRoute

    // Listening
    @Serializable data object Radio : GraceRoute
    @Serializable data class Player(val contentId: String) : GraceRoute
    @Serializable data class LiveSession(val sessionId: String) : GraceRoute
    @Serializable data class PodcastDetail(val podcastId: String) : GraceRoute
    @Serializable data class EpisodePlayer(val episodeId: String) : GraceRoute

    // Community / church
    @Serializable data object Churches : GraceRoute
    @Serializable data class ChurchDetail(val churchId: String) : GraceRoute
    @Serializable data object ChurchPortal : GraceRoute
    @Serializable data object Prayer : GraceRoute
    @Serializable data object Events : GraceRoute
    @Serializable data object Articles : GraceRoute
    @Serializable data object Faith : GraceRoute
}

/**
 * The five primary destinations shown in the floating bottom navigation.
 * Home carries the "listen live" + overview surface, so Radio doesn't need
 * its own tab; Podcasts keeps a dedicated one since it's a distinct habit.
 */
val bottomNavRoutes = listOf(
    GraceRoute.Home,
    GraceRoute.Podcasts,
    GraceRoute.LiveSpaces,
    GraceRoute.Community,
    GraceRoute.Profile,
)
