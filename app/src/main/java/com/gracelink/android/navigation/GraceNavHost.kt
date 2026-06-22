package com.gracelink.android.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
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
import com.gracelink.android.feature.events.EventsScreen
import com.gracelink.android.feature.home.HomeScreen
import com.gracelink.android.feature.library.LibraryScreen
import com.gracelink.android.feature.onboarding.OnboardingScreen
import com.gracelink.android.feature.player.LiveSessionScreen
import com.gracelink.android.feature.player.PlayerScreen
import com.gracelink.android.feature.prayer.PrayerWallScreen
import com.gracelink.android.feature.profile.ProfileScreen
import com.gracelink.android.feature.splash.SplashScreen

/**
 * Root composable rendered by MainActivity. Owns the NavController + Scaffold.
 */
@Composable
fun GraceLinkApp(onContentReady: () -> Unit = {}) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    onContentReady()

    // Hide bottom bar on splash / onboarding / player screens.
    val showBottomBar = currentRoute in setOf(
        GraceDestination.Home::class.qualifiedName,
        GraceDestination.Library::class.qualifiedName,
        GraceDestination.Events::class.qualifiedName,
        GraceDestination.Prayer::class.qualifiedName,
        GraceDestination.Profile::class.qualifiedName,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                GraceBottomBar(currentRoute = currentRoute) { dest ->
                    navController.navigate(dest) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = GraceDestination.Splash,
            ) {
                composable<GraceDestination.Splash> {
                    SplashScreen(onComplete = {
                        navController.navigate(GraceDestination.Home) {
                            popUpTo(GraceDestination.Splash) { inclusive = true }
                        }
                    })
                }
                composable<GraceDestination.Onboarding> {
                    OnboardingScreen(onDone = {
                        navController.navigate(GraceDestination.Home) {
                            popUpTo(GraceDestination.Onboarding) { inclusive = true }
                        }
                    })
                }
                composable<GraceDestination.Home> {
                    HomeScreen(
                        onPlayContent = { id -> navController.navigate(GraceDestination.Player(id)) },
                        onOpenLiveSession = { id -> navController.navigate(GraceDestination.LiveSession(id)) },
                        onSeeAll = { navController.navigate(GraceDestination.Library) },
                    )
                }
                composable<GraceDestination.Library> {
                    LibraryScreen(
                        onPlayContent = { id -> navController.navigate(GraceDestination.Player(id)) },
                    )
                }
                composable<GraceDestination.Events> {
                    EventsScreen(
                        onOpenLiveSession = { id -> navController.navigate(GraceDestination.LiveSession(id)) },
                    )
                }
                composable<GraceDestination.Prayer> {
                    PrayerWallScreen()
                }
                composable<GraceDestination.Profile> {
                    ProfileScreen()
                }
                composable<GraceDestination.Player> { backStackEntry ->
                    val route: GraceDestination.Player = backStackEntry.toRoute()
                    PlayerScreen(
                        contentId = route.contentId,
                        onBack = { navController.popBackStack() },
                        onOpenLiveSession = { id -> navController.navigate(GraceDestination.LiveSession(id)) },
                    )
                }
                composable<GraceDestination.LiveSession> { backStackEntry ->
                    val route: GraceDestination.LiveSession = backStackEntry.toRoute()
                    LiveSessionScreen(
                        sessionId = route.sessionId,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
private fun GraceBottomBar(
    currentRoute: String?,
    onNavigate: (GraceDestination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        bottomNavDestinations.forEach { dest ->
            val (icon, label) = dest.iconAndLabel()
            val selected = currentRoute == dest::class.qualifiedName
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                )
            )
        }
    }
}

private fun GraceDestination.iconAndLabel(): Pair<ImageVector, String> = when (this) {
    GraceDestination.Home    -> Icons.Filled.Home to "Home"
    GraceDestination.Library -> Icons.Filled.LibraryMusic to "Library"
    GraceDestination.Events  -> Icons.Filled.CalendarMonth to "Events"
    GraceDestination.Prayer  -> Icons.Filled.Spa to "Prayer"
    GraceDestination.Profile -> Icons.Filled.Person to "Profile"
    else -> Icons.Filled.Home to ""
}

