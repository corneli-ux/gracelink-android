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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary
import com.gracelink.android.feature.audioconnect.AudioConnectScreen
import com.gracelink.android.feature.articles.ArticlesScreen
import com.gracelink.android.feature.auth.AuthScreen
import com.gracelink.android.feature.churches.ChurchDetailScreen
import com.gracelink.android.feature.churches.ChurchesScreen
import com.gracelink.android.feature.churches.ChurchProfileScreen
import com.gracelink.android.feature.events.EventsScreen
import com.gracelink.android.feature.faith.FaithScreen
import com.gracelink.android.feature.fm.FmScreen
import com.gracelink.android.feature.onboarding.OnboardingScreen
import com.gracelink.android.feature.player.LiveSessionScreen
import com.gracelink.android.feature.player.PlayerScreen
import com.gracelink.android.feature.prayer.PrayerWallScreen
import com.gracelink.android.feature.profile.ProfileScreen
import com.gracelink.android.feature.registration.RegistrationScreen
import com.gracelink.android.feature.splash.SplashScreen
import com.gracelink.android.feature.portal.PortalHubScreen
import com.gracelink.android.feature.podcast.PodcastsScreen
import com.gracelink.android.feature.podcast.PodcastDetailScreen
import com.gracelink.android.feature.churchportal.ChurchPortalScreen
import com.gracelink.android.feature.community.CommunityScreen

/**
 * Single source of truth for GraceLink navigation.
 *
 * Flow after login (new clean design):
 * Splash → Onboarding (first launch) → Auth → Registration (if new) → PortalHub
 *
 * From PortalHub the user enters either:
 * - Members experience (bottom nav: Radio / Podcasts / Live / Community / Profile)
 * - Church Portal (leadership tools)
 *
 * Bottom navigation is a unique floating pill, not the standard Material bar.
 */
@Composable
fun GraceNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomNavRoutes.any { route ->
        currentRoute?.contains(route::class.simpleName ?: "") == true
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
                // ── Pre-auth ──────────────────────────────────────────────
                composable<GraceRoute.Splash> {
                    SplashScreen(
                        onComplete = {
                            navController.navigate(GraceRoute.Onboarding) {
                                popUpTo(GraceRoute.Splash) { inclusive = true }
                            }
                        }
                    )
                }

                composable<GraceRoute.Onboarding> {
                    OnboardingScreen(
                        onDone = {
                            navController.navigate(GraceRoute.Auth) {
                                popUpTo(GraceRoute.Onboarding) { inclusive = true }
                            }
                        }
                    )
                }

                composable<GraceRoute.Auth> {
                    AuthScreen(
                        onSignInComplete = {
                            navController.navigate(GraceRoute.PortalHub) {
                                popUpTo(GraceRoute.Auth) { inclusive = true }
                            }
                        },
                        onNewUserNeedsRegistration = { name, email ->
                            // Store temporarily if needed; RegistrationScreen handles the rest
                            navController.navigate(GraceRoute.Registration) {
                                // keep Auth in stack so user can go back if they cancel
                            }
                        },
                        onRegister = {
                            navController.navigate(GraceRoute.Registration)
                        }
                    )
                }

                composable<GraceRoute.Registration> {
                    RegistrationScreen(
                        onComplete = {
                            navController.navigate(GraceRoute.PortalHub) {
                                popUpTo(GraceRoute.Auth) { inclusive = true }
                            }
                        }
                    )
                }

                // ── Post-login hub (unique entry point) ───────────────────
                composable<GraceRoute.PortalHub> {
                    PortalHubScreen(
                        onEnterMembers = {
                            navController.navigate(GraceRoute.Radio) {
                                popUpTo(GraceRoute.PortalHub) { inclusive = false }
                            }
                        },
                        onEnterChurchPortal = {
                            navController.navigate(GraceRoute.ChurchPortal)
                        },
                        onOpenProfile = {
                            navController.navigate(GraceRoute.Profile)
                        }
                    )
                }

                // ── Bottom-nav destinations ───────────────────────────────
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
                        onNavigateToChurches = {
                            navController.navigate(GraceRoute.Churches)
                        },
                        onNavigateToChurchProfile = {
                            navController.navigate(GraceRoute.ChurchPortal)
                        }
                    )
                }

                // ── Church & detail routes ────────────────────────────────
                composable<GraceRoute.Churches> {
                    ChurchesScreen(
                        onChurchClick = { id -> navController.navigate(GraceRoute.ChurchDetail(id)) }
                    )
                }

                composable<GraceRoute.ChurchDetail> { entry ->
                    val route = entry.toRoute<GraceRoute.ChurchDetail>()
                    ChurchDetailScreen(
                        churchId = route.churchId,
                        onBack = { navController.popBackStack() }
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
                    PrayerWallScreen()
                }

                composable<GraceRoute.Events> {
                    EventsScreen(
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) }
                    )
                }

                composable<GraceRoute.Articles> {
                    ArticlesScreen()
                }

                composable<GraceRoute.Faith> {
                    FaithScreen()
                }

                // ── Player routes ─────────────────────────────────────────
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
                    // Reuse PlayerScreen for episodes or create dedicated later
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
 * Unique floating pill bottom navigation – deliberately different from standard Material NavigationBar.
 * Glass-like surface, gold accent, rounded capsule.
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
    GraceRoute.Radio -> Triple(Icons.Rounded.Radio, Icons.Outlined.Radio, "Radio")
    GraceRoute.Podcasts -> Triple(Icons.Rounded.Podcasts, Icons.Outlined.Podcasts, "Podcasts")
    GraceRoute.LiveSpaces -> Triple(Icons.Rounded.Headphones, Icons.Outlined.Headphones, "Live")
    GraceRoute.Community -> Triple(Icons.Rounded.Groups, Icons.Outlined.Groups, "Community")
    GraceRoute.Profile -> Triple(Icons.Rounded.Person, Icons.Outlined.Person, "Me")
    else -> Triple(Icons.Rounded.Radio, Icons.Outlined.Radio, "")
}
