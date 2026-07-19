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
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.gracelink.android.feature.auth.AuthScreen
import com.gracelink.android.feature.auth.GoogleAuthData
import com.gracelink.android.feature.churches.ChurchDetailScreen
import com.gracelink.android.feature.churches.ChurchesScreen
import com.gracelink.android.feature.churchportal.ChurchPortalScreen
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
 * Single source of truth for GraceLink navigation.
 *
 * Flow:
 *   Splash -> (first launch) Onboarding -> Auth (mandatory) -> Home
 *   Splash -> (returning, not signed in) Auth (mandatory) -> Home
 *   Splash -> (returning, signed in) Home directly
 *
 * Sign-in is REQUIRED before the app is usable -- there is no guest mode.
 * The only exception is the one-time Onboarding screens, which explain the
 * app before asking for an account. Once signed in, Auth/Registration are
 * never shown again unless the person signs out.
 */
@Composable
fun GraceNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authGateVm: AuthGateViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomNavRoutes.any { route ->
        currentRoute?.contains(route::class.simpleName ?: "") == true
    }

    /** After Splash or Onboarding: signed in -> Home, otherwise -> Auth (mandatory gate). */
    fun navigateAfterGateCheck(popRoute: GraceRoute, popInclusive: Boolean = true) {
        scope.launch {
            val destination = if (authGateVm.isSignedIn()) GraceRoute.Home else GraceRoute.Auth
            navController.navigate(destination) {
                popUpTo(popRoute) { inclusive = popInclusive }
            }
        }
    }

    /** After Auth/Registration completes: return to caller if there was one, otherwise go to Home. */
    fun navigateAfterSignIn() {
        val popped = navController.popBackStack(GraceRoute.Auth, true)
        if (!popped) {
            navController.navigate(GraceRoute.Home) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
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
                                navigateAfterGateCheck(GraceRoute.Splash)
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
                            navigateAfterGateCheck(GraceRoute.Onboarding)
                        }
                    )
                }

                composable<GraceRoute.Auth> {
                    AuthScreen(
                        onSignInComplete = { navigateAfterSignIn() },
                        onNewUserNeedsRegistration = { _, _ ->
                            navController.navigate(GraceRoute.Registration)
                        },
                        onRegister = {
                            navController.navigate(GraceRoute.Registration)
                        }
                    )
                }

                composable<GraceRoute.Registration> {
                    RegistrationScreen(
                        onComplete = {
                            GoogleAuthData.clear()
                            navigateAfterSignIn()
                        }
                    )
                }

                // -- Home (single unified hub, requires sign-in to reach) ----
                composable<GraceRoute.Home> {
                    HomeScreen(
                        onPlayContent = { id -> navController.navigate(GraceRoute.Player(id)) },
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) },
                        onOpenRadio = { navController.navigate(GraceRoute.Radio) },
                        onOpenPodcasts = { navController.navigate(GraceRoute.Podcasts) },
                        onOpenCommunity = { navController.navigate(GraceRoute.Community) },
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
                        onOpenFaith = { navController.navigate(GraceRoute.Faith) }
                    )
                }

                composable<GraceRoute.Profile> {
                    ProfileScreen(
                        onNavigateToFaith = { navController.navigate(GraceRoute.Faith) },
                        onNavigateToArticles = { navController.navigate(GraceRoute.Articles) },
                        onNavigateToChurches = { navController.navigate(GraceRoute.Churches) },
                        onNavigateToChurchPortal = { navController.navigate(GraceRoute.ChurchPortal) },
                        onSignedOut = {
                            navController.navigate(GraceRoute.Auth) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            }
                        },
                    )
                }

                // -- Church & detail routes ----------------------------------
                composable<GraceRoute.Churches> {
                    ChurchesScreen(
                        onChurchClick = { id -> navController.navigate(GraceRoute.ChurchDetail(id)) },
                        onRequireSignIn = { navController.navigate(GraceRoute.Auth) }
                    )
                }

                composable<GraceRoute.ChurchDetail> { entry ->
                    val route = entry.toRoute<GraceRoute.ChurchDetail>()
                    ChurchDetailScreen(
                        churchId = route.churchId,
                        onBack = { navController.popBackStack() },
                        onRequireSignIn = { navController.navigate(GraceRoute.Auth) }
                    )
                }

                composable<GraceRoute.ChurchPortal> {
                    ChurchPortalScreen(
                        onBack = { navController.popBackStack() },
                        onManageMembers = { /* future */ },
                        onScheduleRadio = { navController.navigate(GraceRoute.Radio) },
                        onStartSpace = { navController.navigate(GraceRoute.LiveSpaces) }
                    )
                }

                composable<GraceRoute.Prayer> {
                    PrayerWallScreen(
                        onRequireSignIn = { navController.navigate(GraceRoute.Auth) }
                    )
                }

                composable<GraceRoute.Events> {
                    EventsScreen(
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) }
                    )
                }

                composable<GraceRoute.Articles> {
                    ArticlesScreen(
                        onRequireSignIn = { navController.navigate(GraceRoute.Auth) }
                    )
                }

                composable<GraceRoute.Faith> {
                    FaithScreen(
                        onRequireSignIn = { navController.navigate(GraceRoute.Auth) }
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
