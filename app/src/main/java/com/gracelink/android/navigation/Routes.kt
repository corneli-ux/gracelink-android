package com.gracelink.android.navigation

import kotlinx.serialization.Serializable

sealed interface GraceRoute : java.io.Serializable {
    @Serializable data object Splash : GraceRoute
    @Serializable data object Onboarding : GraceRoute
    @Serializable data object Home : GraceRoute
    @Serializable data object Library : GraceRoute
    @Serializable data object Fm : GraceRoute
    @Serializable data object Events : GraceRoute
    @Serializable data object Prayer : GraceRoute
    @Serializable data object Profile : GraceRoute
    @Serializable data class Player(val contentId: String) : GraceRoute
    @Serializable data class LiveSession(val sessionId: String) : GraceRoute
}

val bottomNavRoutes = listOf(
    GraceRoute.Home, GraceRoute.Library, GraceRoute.Fm, GraceRoute.Events, GraceRoute.Prayer, GraceRoute.Profile,
)
