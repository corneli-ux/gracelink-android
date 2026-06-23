package com.gracelink.android.navigation

import kotlinx.serialization.Serializable

sealed interface GraceRoute : java.io.Serializable {
    @Serializable data object Splash : GraceRoute
    @Serializable data object Onboarding : GraceRoute
    @Serializable data object Auth : GraceRoute
    @Serializable data object Registration : GraceRoute
    @Serializable data object Home : GraceRoute
    @Serializable data object Library : GraceRoute
    @Serializable data object Fm : GraceRoute
    @Serializable data object Events : GraceRoute
    @Serializable data object AudioConnect : GraceRoute
    @Serializable data object Prayer : GraceRoute
    @Serializable data object Articles : GraceRoute
    @Serializable data object Faith : GraceRoute
    @Serializable data object Churches : GraceRoute
    @Serializable data object Profile : GraceRoute
    @Serializable data class Player(val contentId: String) : GraceRoute
    @Serializable data class LiveSession(val sessionId: String) : GraceRoute
}

// 7 main tabs — Events and Faith accessible from Home/Profile
val bottomNavRoutes = listOf(
    GraceRoute.Home, GraceRoute.Library, GraceRoute.Fm,
    GraceRoute.AudioConnect, GraceRoute.Prayer,
    GraceRoute.Articles, GraceRoute.Profile,
)
