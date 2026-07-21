package com.gracelink.android.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material3.Icon
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gracelink.android.core.AppPrefs
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.core.theme.TextSecondary
import com.gracelink.android.feature.audioconnect.AudioConnectScreen
import com.gracelink.android.feature.articles.ArticlesScreen
import com.gracelink.android.feature.churches.ChurchDetailScreen
import com.gracelink.android.feature.churches.ChurchesScreen
import com.gracelink.android.feature.churchportal.ChurchPortalScreen
import com.gracelink.android.feature.pastorportal.PastorPortalScreen
import com.gracelink.android.feature.community.CommunityScreen
import com.gracelink.android.feature.events.EventsScreen
import com.gracelink.android.feature.faith.FaithScreen
import com.gracelink.android.feature.fm.FmScreen
import com.gracelink.android.feature.home.HomeScreen
import com.gracelink.android.feature.onboarding.OnboardingScreen
import com.gracelink.android.feature.player.LiveSessionScreen
import com.gracelink.android.feature.player.PlayerScreen
import com.gracelink.android.feature.podcast.PodcastDetailScreen
import com.gracelink.android.feature.podcast.PodcastsScreen
import com.gracelink.android.feature.prayer.PrayerWallScreen
import com.gracelink.android.feature.profile.ProfileScreen
import com.gracelink.android.feature.registration.RegistrationScreen
import com.gracelink.android.feature.splash.SplashScreen
import kotlinx.coroutines.launch

/**
 * Single source of truth for Faith Link navigation.
 *
 * Flow:
 *   Splash -> (first launch) Onboarding -> Set Up Profile (mandatory) -> Home/Portal
 *   Splash -> (returning, no profile yet) Set Up Profile (mandatory) -> Home/Portal
 *   Splash -> (returning, profile exists) straight into Home, or the
 *             matching Church/Pastor portal if that's the account's role
 *
 * "Set Up Profile" is NOT a credential login (no password) -- it's a
 * mandatory one-time step that asks for a name and a role (Member /
 * Individual Pastor / Church), so every identity-dependent feature
 * (prayers, articles, portals) has something real to attach to. Once
 * set up, the person lands directly in the surface that matches their
 * role instead of always seeing the generic member Home.
 */
@Composable
fun GraceNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profileGateVm: ProfileGateViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomNavRoutes.any { route ->
        currentRoute?.contains(route::class.simpleName ?: "") == true
    }

    /**
     * Checks whether a profile already exists (or can be restored -- see
     * ProfileGateViewModel) and routes accordingly, popping back to
     * [popRoute]. Home is always established as the base of the stack
     * first -- previously a Church/Pastor account landed directly in
     * their portal with NOTHING underneath it, so swiping back closed
     * the whole app instead of going anywhere. Now swiping back from a
     * portal always lands on Home.
     */
    /**
     * Checks whether a profile already exists (or can be restored -- see
     * ProfileGateViewModel) and routes accordingly, popping back to
     * [popRoute]. Always lands on Home -- previously this auto-navigated
     * Church/Pastor accounts straight into their portal on top of Home,
     * which looked like "the app opens directly into Church Portal"
     * instead of showing Home first. The portal is one tap away from
     * Home/Profile for whoever wants it, not the forced landing screen.
     */
    fun routeByProfile(popRoute: GraceRoute, ifNoProfile: GraceRoute) {
        scope.launch {
            val type = profileGateVm.restoreOrCheckProfile()
            val destination = if (type == null) ifNoProfile else GraceRoute.Home
            navController.navigate(destination) { popUpTo(popRoute) { inclusive = true } }
        }
    }

    Scaffold(
        containerColor = Obsidian,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                GraceFloatingBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Obsidian)
        ) {
            NavHost(
                navController = navController,
                startDestination = GraceRoute.Splash,
                modifier = Modifier.fillMaxSize()
            ) {
                // -- Pre-auth / first-run -----------------------------------
                composable<GraceRoute.Splash> {
                    SplashScreen(
                        onComplete = {
                            if (AppPrefs.hasOnboarded(context)) {
                                routeByProfile(GraceRoute.Splash, GraceRoute.Auth)
                            } else {
                                navController.navigate(GraceRoute.Onboarding) {
                                    popUpTo(GraceRoute.Splash) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable<GraceRoute.Onboarding> {
                    OnboardingScreen(
                        onDone = {
                            AppPrefs.setOnboarded(context)
                            routeByProfile(GraceRoute.Onboarding, GraceRoute.Auth)
                        }
                    )
                }

                composable<GraceRoute.Auth> {
                    com.gracelink.android.feature.auth.AuthScreen(
                        onSignInComplete = {
                            // Existing Firebase account signed back in -- if a local
                            // profile already exists (they've done this before), skip
                            // straight to their portal instead of Registration again.
                            routeByProfile(GraceRoute.Auth, GraceRoute.Registration)
                        },
                        onNewUserNeedsRegistration = { _, _ ->
                            navController.navigate(GraceRoute.Registration) {
                                popUpTo(GraceRoute.Auth) { inclusive = true }
                            }
                        },
                    )
                }

                composable<GraceRoute.Registration> {
                    RegistrationScreen(
                        onComplete = { _ ->
                            navController.navigate(GraceRoute.Home) {
                                popUpTo(GraceRoute.Registration) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }

                // -- Home (single unified hub, requires sign-in to reach) ----
                composable<GraceRoute.Home> {
                    HomeScreen(
                        onPlayContent = { id -> navController.navigate(GraceRoute.Player(id)) },
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) },
                        onOpenForum = { navController.navigate(GraceRoute.Forum) },
                    )
                }

                composable<GraceRoute.Radio> {
                    FmScreen()
                }

                composable<GraceRoute.Podcasts> {
                    PodcastsScreen(
                        onOpenPodcast = { id -> navController.navigate(GraceRoute.PodcastDetail(id)) },
                        onPlayEpisode = { id -> navController.navigate(GraceRoute.EpisodePlayer(id)) }
                    )
                }

                composable<GraceRoute.LiveSpaces> {
                    AudioConnectScreen()
                }

                composable<GraceRoute.Community> {
                    CommunityScreen(
                        onOpenChurches = { navController.navigate(GraceRoute.Churches) },
                        onOpenPrayer = { navController.navigate(GraceRoute.Prayer) },
                        onOpenEvents = { navController.navigate(GraceRoute.Events) },
                        onOpenArticles = { navController.navigate(GraceRoute.Articles) },
                        onOpenFaith = { navController.navigate(GraceRoute.Faith) },
                        onOpenForum = { navController.navigate(GraceRoute.Forum) },
                    )
                }

                composable<GraceRoute.Profile> {
                    ProfileScreen(
                        onNavigateToFaith = { navController.navigate(GraceRoute.Faith) },
                        onNavigateToArticles = { navController.navigate(GraceRoute.Articles) },
                        onNavigateToChurches = { navController.navigate(GraceRoute.Churches) },
                        onNavigateToChurchPortal = { navController.navigate(GraceRoute.ChurchPortal) },
                        onNavigateToPastorPortal = { navController.navigate(GraceRoute.PastorPortal) },
                        onSetupProfile = { navController.navigate(GraceRoute.Registration) },
                        onSignedOut = {
                            navController.navigate(GraceRoute.Auth) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            }
                        },
                        onOpenDownloads = { navController.navigate(GraceRoute.DownloadsManager) },
                    )
                }

                composable<GraceRoute.DownloadsManager> {
                    com.gracelink.android.feature.downloads.DownloadsManagerScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                // -- Church & detail routes ----------------------------------
                composable<GraceRoute.Churches> {
                    ChurchesScreen(
                        onChurchClick = { id -> navController.navigate(GraceRoute.ChurchDetail(id)) },
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) }
                    )
                }

                composable<GraceRoute.ChurchDetail> { entry ->
                    val route = entry.toRoute<GraceRoute.ChurchDetail>()
                    ChurchDetailScreen(
                        churchId = route.churchId,
                        onBack = { navController.popBackStack() },
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) }
                    )
                }

                composable<GraceRoute.ChurchPortal> {
                    ChurchPortalScreen(
                        onBack = { navController.popBackStack() },
                        onScheduleRadio = { navController.navigate(GraceRoute.RadioBooking) },
                        onStartSpace = { navController.navigate(GraceRoute.LiveSpaces) },
                        onOpenPodcasts = { navController.navigate(GraceRoute.PodcastCreate) },
                        onWriteArticle = { navController.navigate(GraceRoute.Articles) },
                        onCreateEvent = { navController.navigate(GraceRoute.EventCreate) },
                        onEditProfile = { navController.navigate(GraceRoute.ChurchEditProfile) },
                        onViewMembers = { navController.navigate(GraceRoute.ChurchMembers) },
                        onOpenAnnouncements = { navController.navigate(GraceRoute.ChurchAnnouncements) },
                        onOpenGroups = { navController.navigate(GraceRoute.ChurchGroups) },
                        onOpenLeadership = { navController.navigate(GraceRoute.ChurchLeadership) },
                        onOpenMinistries = { navController.navigate(GraceRoute.ChurchMinistries) },
                        onOpenEventRsvp = { id -> navController.navigate(GraceRoute.EventRsvp(id)) },
                        onOpenServiceTimes = { navController.navigate(GraceRoute.ChurchServiceTimes) },
                        onOpenModerationLog = { navController.navigate(GraceRoute.ModerationLog) },
                        onOpenInsights = { navController.navigate(GraceRoute.ChurchInsights) },
                    )
                }

                composable<GraceRoute.ChurchEditProfile> {
                    com.gracelink.android.feature.churchportal.ChurchEditProfileScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<GraceRoute.ChurchMembers> {
                    com.gracelink.android.feature.members.ChurchMembersScreen(
                        onBack = { navController.popBackStack() },
                        onMemberClick = { id -> navController.navigate(GraceRoute.MemberDetail(id)) },
                    )
                }

                composable<GraceRoute.MemberDetail> { entry ->
                    val route = entry.toRoute<GraceRoute.MemberDetail>()
                    com.gracelink.android.feature.churchportal.MemberDetailScreen(
                        memberId = route.memberId,
                        onBack = { navController.popBackStack() },
                        onMessage = { uid, name -> navController.navigate(GraceRoute.DirectChat(uid, name)) },
                    )
                }

                composable<GraceRoute.ChurchAnnouncements> {
                    com.gracelink.android.feature.announcements.AnnouncementsScreen(
                        onBack = { navController.popBackStack() },
                        onCreate = { navController.navigate(GraceRoute.CreateAnnouncement) },
                    )
                }

                composable<GraceRoute.CreateAnnouncement> {
                    com.gracelink.android.feature.announcements.CreateAnnouncementScreen(
                        onBack = { navController.popBackStack() },
                        onCreated = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.ChurchGroups> {
                    com.gracelink.android.feature.groups.GroupsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenGroup = { id -> navController.navigate(GraceRoute.GroupDetail(id)) },
                        onCreate = { navController.navigate(GraceRoute.CreateGroup) },
                    )
                }

                composable<GraceRoute.GroupDetail> { entry ->
                    val route = entry.toRoute<GraceRoute.GroupDetail>()
                    com.gracelink.android.feature.groups.GroupDetailScreen(
                        groupId = route.groupId,
                        onBack = { navController.popBackStack() },
                        onOpenChat = { navController.navigate(GraceRoute.GroupChat(route.groupId)) },
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) },
                    )
                }

                composable<GraceRoute.GroupChat> { entry ->
                    val route = entry.toRoute<GraceRoute.GroupChat>()
                    com.gracelink.android.feature.groups.GroupChatScreen(
                        groupId = route.groupId,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.CreateGroup> {
                    com.gracelink.android.feature.groups.CreateGroupScreen(
                        onBack = { navController.popBackStack() },
                        onCreated = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.ChurchLeadership> {
                    com.gracelink.android.feature.leadership.LeadershipScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.ChurchMinistries> {
                    com.gracelink.android.feature.ministries.MinistriesScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.EventRsvp> { entry ->
                    val route = entry.toRoute<GraceRoute.EventRsvp>()
                    com.gracelink.android.feature.events.EventRsvpScreen(
                        eventId = route.eventId,
                        onBack = { navController.popBackStack() },
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) },
                    )
                }

                composable<GraceRoute.ChurchServiceTimes> {
                    com.gracelink.android.feature.servicetimes.ServiceTimesScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.ModerationLog> {
                    com.gracelink.android.feature.moderation.ModerationScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.ChurchInsights> {
                    com.gracelink.android.feature.insights.InsightsScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.PastorInsights> {
                    com.gracelink.android.feature.insights.PastorInsightsScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.DirectChat> { entry ->
                    val route = entry.toRoute<GraceRoute.DirectChat>()
                    com.gracelink.android.feature.messages.DirectChatScreen(
                        otherUserId = route.otherUserId,
                        otherName = route.otherName,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.PastorPortal> {
                    PastorPortalScreen(
                        onBack = { navController.popBackStack() },
                        onScheduleRadio = { navController.navigate(GraceRoute.RadioBooking) },
                        onStartSpace = { navController.navigate(GraceRoute.LiveSpaces) },
                        onOpenPodcasts = { navController.navigate(GraceRoute.PodcastCreate) },
                        onWriteArticle = { navController.navigate(GraceRoute.Articles) },
                        onCreateEvent = { navController.navigate(GraceRoute.EventCreate) },
                        onOpenInsights = { navController.navigate(GraceRoute.PastorInsights) },
                    )
                }

                composable<GraceRoute.RadioBooking> {
                    com.gracelink.android.feature.radiobooking.RadioBookingScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<GraceRoute.PodcastCreate> {
                    com.gracelink.android.feature.podcast.PodcastCreateScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<GraceRoute.EventCreate> {
                    com.gracelink.android.feature.events.EventCreateScreen(
                        onBack = { navController.popBackStack() },
                        onCreated = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.Prayer> {
                    PrayerWallScreen(
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) }
                    )
                }

                composable<GraceRoute.Events> {
                    EventsScreen(
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) }
                    )
                }

                composable<GraceRoute.Articles> {
                    ArticlesScreen(
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) },
                        onOpenArticle = { id -> navController.navigate(GraceRoute.ArticleDetail(id)) },
                    )
                }

                composable<GraceRoute.ArticleDetail> { entry ->
                    val route = entry.toRoute<GraceRoute.ArticleDetail>()
                    com.gracelink.android.feature.articles.ArticleDetailScreen(
                        articleId = route.articleId,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<GraceRoute.Faith> {
                    FaithScreen(
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) }
                    )
                }

                composable<GraceRoute.Forum> {
                    com.gracelink.android.feature.forum.ForumScreen(
                        onOpenQuestion = { id -> navController.navigate(GraceRoute.QuestionDetail(id)) },
                        onAskQuestion = { navController.navigate(GraceRoute.AskQuestion) },
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) },
                    )
                }

                composable<GraceRoute.AskQuestion> {
                    com.gracelink.android.feature.forum.AskQuestionScreen(
                        onBack = { navController.popBackStack() },
                        onAsked = { id ->
                            navController.navigate(GraceRoute.QuestionDetail(id)) {
                                popUpTo(GraceRoute.AskQuestion) { inclusive = true }
                            }
                        },
                    )
                }

                composable<GraceRoute.QuestionDetail> { entry ->
                    val route = entry.toRoute<GraceRoute.QuestionDetail>()
                    com.gracelink.android.feature.forum.QuestionDetailScreen(
                        questionId = route.questionId,
                        onBack = { navController.popBackStack() },
                        onRequireSignIn = { navController.navigate(GraceRoute.Registration) },
                    )
                }

                // -- Player routes --------------------------------------------
                composable<GraceRoute.Player> { entry ->
                    val route = entry.toRoute<GraceRoute.Player>()
                    PlayerScreen(
                        contentId = route.contentId,
                        onBack = { navController.popBackStack() },
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) }
                    )
                }

                composable<GraceRoute.LiveSession> { entry ->
                    val route = entry.toRoute<GraceRoute.LiveSession>()
                    LiveSessionScreen(
                        sessionId = route.sessionId,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<GraceRoute.PodcastDetail> { entry ->
                    val route = entry.toRoute<GraceRoute.PodcastDetail>()
                    PodcastDetailScreen(
                        podcastId = route.podcastId,
                        onBack = { navController.popBackStack() },
                        onPlayEpisode = { id -> navController.navigate(GraceRoute.EpisodePlayer(id)) }
                    )
                }

                composable<GraceRoute.EpisodePlayer> { entry ->
                    val route = entry.toRoute<GraceRoute.EpisodePlayer>()
                    // Reuse PlayerScreen for episodes; a dedicated episode player can follow later.
                    PlayerScreen(
                        contentId = route.episodeId,
                        onBack = { navController.popBackStack() },
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) }
                    )
                }
            }
        }
    }
}

/**
 * Unique floating pill bottom navigation -- deliberately different from the
 * standard Material NavigationBar. Glass-like surface, gold accent, rounded
 * capsule.
 */
@Composable
private fun GraceFloatingBottomBar(
    currentRoute: String?,
    onNavigate: (GraceRoute) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Gold400.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.08f),
                            Gold400.copy(alpha = 0.35f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                ),
            color = Slate900.copy(alpha = 0.92f),
            tonalElevation = 0.dp,
            shadowElevation = 12.dp
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                bottomNavRoutes.forEach { route ->
                    val (selectedIcon, unselectedIcon, label) = route.icons()
                    val selected = currentRoute?.contains(route::class.simpleName ?: "") == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = { onNavigate(route) },
                        icon = {
                            Icon(
                                imageVector = if (selected) selectedIcon else unselectedIcon,
                                contentDescription = label,
                                tint = if (selected) Gold500 else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Gold500 else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold500,
                            selectedTextColor = Gold500,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Gold500.copy(alpha = 0.12f)
                        ),
                        alwaysShowLabel = true
                    )
                }
            }
        }
    }
}

private fun GraceRoute.icons(): Triple<ImageVector, ImageVector, String> = when (this) {
    GraceRoute.Home -> Triple(Icons.Rounded.Home, Icons.Outlined.Home, "Home")
    GraceRoute.Podcasts -> Triple(Icons.Rounded.Podcasts, Icons.Outlined.Podcasts, "Podcasts")
    GraceRoute.LiveSpaces -> Triple(Icons.Rounded.Headphones, Icons.Outlined.Headphones, "Live")
    GraceRoute.Community -> Triple(Icons.Rounded.Groups, Icons.Outlined.Groups, "Community")
    GraceRoute.Profile -> Triple(Icons.Rounded.Person, Icons.Outlined.Person, "Me")
    else -> Triple(Icons.Rounded.Home, Icons.Outlined.Home, "")
}
