package com.gracelink.android.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gracelink.android.feature.audioconnect.AudioConnectScreen
import com.gracelink.android.feature.articles.ArticlesScreen
import com.gracelink.android.feature.churches.ChurchesScreen
import com.gracelink.android.feature.faith.FaithScreen
import com.gracelink.android.feature.registration.RegistrationScreen
import com.gracelink.android.feature.auth.AuthScreen
import com.gracelink.android.feature.events.EventsScreen
import com.gracelink.android.feature.fm.FmScreen
import com.gracelink.android.feature.home.HomeScreen
import com.gracelink.android.feature.library.LibraryScreen
import com.gracelink.android.feature.onboarding.OnboardingScreen
import com.gracelink.android.feature.player.LiveSessionScreen
import com.gracelink.android.feature.player.PlayerScreen
import com.gracelink.android.feature.prayer.PrayerWallScreen
import com.gracelink.android.feature.profile.ProfileScreen
import com.gracelink.android.feature.splash.SplashScreen

@Composable
fun GraceNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val showBottomBar = currentRoute in bottomNavRoutes.map { it::class.qualifiedName }.toSet()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                GraceBottomBar(currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            NavHost(navController, startDestination = GraceRoute.Splash) {
                composable<GraceRoute.Splash> {
                    SplashScreen { navController.navigate(GraceRoute.Onboarding) { popUpTo(GraceRoute.Splash) { inclusive = true } } }
                }
                composable<GraceRoute.Onboarding> {
                    OnboardingScreen { navController.navigate(GraceRoute.Auth) { popUpTo(GraceRoute.Onboarding) { inclusive = true } } }
                }
                composable<GraceRoute.Auth> {
                    AuthScreen(
                        onSignInComplete = { navController.navigate(GraceRoute.Home) { popUpTo(GraceRoute.Auth) { inclusive = true } } },
                        onNewUserNeedsRegistration = { name, email ->
                            navController.navigate(GraceRoute.Registration) {
                                popUpTo(GraceRoute.Auth) { inclusive = false }
                            }
                        },
                        onRegister = { navController.navigate(GraceRoute.Registration) },
                    )
                }
                composable<GraceRoute.Home> {
                    HomeScreen(
                        onPlayContent = { id -> navController.navigate(GraceRoute.Player(id)) },
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) },
                        onSeeAll = { navController.navigate(GraceRoute.Library) },
                    )
                }
                composable<GraceRoute.Library> {
                    LibraryScreen(onPlayContent = { id -> navController.navigate(GraceRoute.Player(id)) })
                }
                composable<GraceRoute.Fm> {
                    FmScreen()
                }
                composable<GraceRoute.Events> {
                    EventsScreen(onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) })
                }
                composable<GraceRoute.Prayer> { PrayerWallScreen() }
                composable<GraceRoute.AudioConnect> { AudioConnectScreen() }
                composable<GraceRoute.Articles> { ArticlesScreen() }
                composable<GraceRoute.Faith> { FaithScreen() }
                composable<GraceRoute.Churches> { ChurchesScreen() }
                composable<GraceRoute.Registration> {
                    RegistrationScreen(
                        onComplete = {
                            com.gracelink.android.feature.auth.GoogleAuthData.clear()
                            navController.navigate(GraceRoute.Home) {
                                popUpTo(GraceRoute.Registration) { inclusive = true }
                            }
                        },
                    )
                }
                composable<GraceRoute.Profile> {
                    ProfileScreen(
                        onNavigateToFaith = { navController.navigate(GraceRoute.Faith) },
                        onNavigateToArticles = { navController.navigate(GraceRoute.Articles) },
                        onNavigateToChurches = { navController.navigate(GraceRoute.Churches) },
                    )
                }
                composable<GraceRoute.Player> { entry ->
                    val route = entry.toRoute<GraceRoute.Player>()
                    PlayerScreen(
                        contentId = route.contentId,
                        onBack = { navController.popBackStack() },
                        onOpenLiveSession = { id -> navController.navigate(GraceRoute.LiveSession(id)) },
                    )
                }
                composable<GraceRoute.LiveSession> { entry ->
                    val route = entry.toRoute<GraceRoute.LiveSession>()
                    LiveSessionScreen(sessionId = route.sessionId, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun GraceBottomBar(currentRoute: String?, onNavigate: (GraceRoute) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        bottomNavRoutes.forEach { route ->
            val (selectedIcon, unselectedIcon, label) = route.icons()
            val selected = currentRoute == route::class.qualifiedName
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(route) },
                icon = { Icon(if (selected) selectedIcon else unselectedIcon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                )
            )
        }
    }
}

private fun GraceRoute.icons(): Triple<ImageVector, ImageVector, String> = when (this) {
    GraceRoute.Home -> Triple(Icons.Rounded.Home, Icons.Outlined.Home, "Home")
    GraceRoute.Library -> Triple(Icons.Rounded.LibraryMusic, Icons.Outlined.LibraryMusic, "Library")
    GraceRoute.Fm -> Triple(Icons.Rounded.Radio, Icons.Outlined.Radio, "FM")
    GraceRoute.AudioConnect -> Triple(Icons.Rounded.Podcasts, Icons.Outlined.Podcasts, "Connect")
    GraceRoute.Prayer -> Triple(Icons.Rounded.Spa, Icons.Outlined.Spa, "Prayer")
    GraceRoute.Articles -> Triple(Icons.Rounded.Article, Icons.Outlined.Article, "Articles")
    GraceRoute.Profile -> Triple(Icons.Rounded.Person, Icons.Outlined.Person, "Profile")
    else -> Triple(Icons.Rounded.Home, Icons.Outlined.Home, "")
}
