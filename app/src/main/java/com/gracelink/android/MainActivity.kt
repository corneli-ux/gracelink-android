package com.gracelink.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gracelink.android.core.designsystem.theme.GraceLinkTheme
import com.gracelink.android.navigation.GraceLinkApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire Compose UI.
 *
 * - Installs the Android 12+ system splash screen and hands off to Compose.
 * - Enables edge-to-edge so the deep slate background bleeds under the status bar.
 * - Delegates all rendering to [GraceLinkApp] (NavHost + scaffold).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called *before* super.onCreate to hook into the system splash.
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Edge-to-edge: status bar transparent, content draws behind it.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
        )

        // Keep splash visible while the first frame of Compose prepares.
        var contentReady = false
        splash.setKeepOnScreenCondition { !contentReady }

        setContent {
            GraceLinkTheme {
                GraceLinkApp(onContentReady = { contentReady = true })
            }
        }
    }
}
