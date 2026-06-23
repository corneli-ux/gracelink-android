package com.gracelink.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gracelink.android.core.designsystem.theme.GraceLinkTheme
import com.gracelink.android.navigation.GraceLinkApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire Compose UI.
 *
 * - Enables edge-to-edge so the deep slate background bleeds under the status bar.
 * - Delegates all rendering to [GraceLinkApp] (NavHost + scaffold).
 *
 * We deliberately do NOT use [androidx.core.splashscreen.SplashScreen] here —
 * that library requires the activity theme to extend Theme.SplashScreen, and
 * any mismatch causes an immediate IllegalArgumentException crash on API < 31.
 * The [SplashScreen] composable handles the visual splash animation instead.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("GraceLinkLaunch", "MainActivity.onCreate: START")
        super.onCreate(savedInstanceState)
        Log.d("GraceLinkLaunch", "MainActivity.onCreate: after super")

        try {
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
            Log.d("GraceLinkLaunch", "MainActivity.onCreate: after enableEdgeToEdge")
        } catch (e: Throwable) {
            Log.e("GraceLinkLaunch", "enableEdgeToEdge failed", e)
        }

        try {
            setContent {
                GraceLinkTheme {
                    GraceLinkApp()
                }
            }
            Log.d("GraceLinkLaunch", "MainActivity.onCreate: after setContent")
        } catch (e: Throwable) {
            Log.e("GraceLinkLaunch", "setContent failed", e)
            throw e
        }
    }
}
