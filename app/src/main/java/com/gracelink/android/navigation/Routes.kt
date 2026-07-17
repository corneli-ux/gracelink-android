package com.gracelink.android.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for GraceLink.
 * Clean post-login flow: Auth → PortalHub → Members / Church portals + core features.
 * Bottom nav is intentionally limited to 5 high-signal destinations for a unique, uncluttered experience.
 */
@Serializable
sealed interface GraceRoute {

    // Pre-auth
    @Serializable data object Splash : GraceRoute
    @Serializable data object Onboarding : GraceRoute
    @Serializable data object Auth : GraceRoute
    @Serializable data object Registration : GraceRoute

    // Post-auth hub (new unique entry after login)
    @Serializable data object PortalHub : GraceRoute

    // Core feature destinations (bottom nav)
    @Serializable data object Radio : GraceRoute          // 24/7 Faith FM with schedule rotation
    @Serializable data object Podcasts : GraceRoute       // Dedicated podcast experience
    @Serializable data object LiveSpaces : GraceRoute     // Audio live like X Spaces
    @Serializable data object Community : GraceRoute      // Churches + Prayer + Events
    @Serializable data object Profile : GraceRoute

    // Nested / detail
    @Serializable data object Churches : GraceRoute
    @Serializable data class ChurchDetail(val churchId: String) : GraceRoute
    @Serializable data object ChurchPortal : GraceRoute   // Admin / leadership portal
    @Serializable data object Prayer : GraceRoute
    @Serializable data object Events : GraceRoute
    @Serializable data object Articles : GraceRoute
    @Serializable data object Faith : GraceRoute
    @Serializable data class Player(val contentId: String) : GraceRoute
    @Serializable data class LiveSession(val sessionId: String) : GraceRoute
    @Serializable data class PodcastDetail(val podcastId: String) : GraceRoute
    @Serializable data class EpisodePlayer(val episodeId: String) : GraceRoute
}

/**
 * The five primary destinations shown in the floating bottom navigation.
 * Kept deliberately short and memorable for a clean, distinctive feel.
 */
val bottomNavRoutes = listOf(
    GraceRoute.Radio,
    GraceRoute.Podcasts,
    GraceRoute.LiveSpaces,
    GraceRoute.Community,
    GraceRoute.Profile
)
