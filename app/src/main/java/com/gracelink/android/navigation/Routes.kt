package com.gracelink.android.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for Faith Link.
 *
 * Flow: Splash -> Onboarding (first launch only) -> Auth (create/sign in
 * with Google) -> Registration (pick account type + enter details) ->
 * Home / Church Portal / Pastor Studio, matching the chosen type.
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
    @Serializable data object Timeline : GraceRoute

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
    @Serializable data object ChurchEditProfile : GraceRoute
    @Serializable data object ChurchMembers : GraceRoute
    @Serializable data object PastorPortal : GraceRoute
    @Serializable data object Prayer : GraceRoute
    @Serializable data object Events : GraceRoute
    @Serializable data object Articles : GraceRoute
    @Serializable data class ArticleDetail(val articleId: String) : GraceRoute
    @Serializable data object Faith : GraceRoute
    @Serializable data object Forum : GraceRoute
    @Serializable data object AskQuestion : GraceRoute
    @Serializable data class QuestionDetail(val questionId: String) : GraceRoute

    // Church/Pastor content creation & booking
    @Serializable data object RadioBooking : GraceRoute
    @Serializable data object PodcastCreate : GraceRoute
    @Serializable data object EventCreate : GraceRoute
    @Serializable data object WritePost : GraceRoute

    // Church administration (roles, announcements, groups, RSVP, leadership, ministries)
    @Serializable data object ChurchAnnouncements : GraceRoute
    @Serializable data object CreateAnnouncement : GraceRoute
    @Serializable data object ChurchGroups : GraceRoute
    @Serializable data class GroupDetail(val groupId: String) : GraceRoute
    @Serializable data object CreateGroup : GraceRoute
    @Serializable data class GroupChat(val groupId: String) : GraceRoute
    @Serializable data object ChurchLeadership : GraceRoute
    @Serializable data object ChurchMinistries : GraceRoute
    @Serializable data object CreateMinistry : GraceRoute
    @Serializable data object ChurchServiceTimes : GraceRoute
    @Serializable data class EventRsvp(val eventId: String) : GraceRoute
    @Serializable data class MemberDetail(val memberId: String) : GraceRoute
    @Serializable data object ModerationLog : GraceRoute
    @Serializable data object ChurchInsights : GraceRoute
    @Serializable data object PastorInsights : GraceRoute
    @Serializable data class DirectChat(val otherUserId: String, val otherName: String) : GraceRoute
    @Serializable data object DownloadsManager : GraceRoute
}

/**
 * The five primary destinations shown in the floating bottom navigation.
 * Home carries the "listen live" + overview surface, so Radio doesn't need
 * its own tab; Podcasts keeps a dedicated one since it's a distinct habit.
 */
val bottomNavRoutes = listOf(
    GraceRoute.Home,
    GraceRoute.Timeline,
    GraceRoute.Podcasts,
    GraceRoute.LiveSpaces,
    GraceRoute.Community,
    GraceRoute.Profile,
)
