package com.gracelink.android

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * GraceLink Application entry point.
 *
 * Hilt bootstraps the dependency graph here. We deliberately keep this lean —
 * any global one-time setup (analytics, crashlytics, work manager config)
 * should live in a dedicated initializer and be called from [onCreate].
 *
 * A default uncaught-exception handler is installed so any crash stack trace
 * is written to logcat under tag "GraceLinkCrash" — useful when debugging on
 * a physical device without Android Studio attached.
 */
@HiltAndroidApp
class GraceLinkApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Crash logger — writes the full stack trace to logcat before the
        // process dies. Replace with Crashlytics once Firebase is wired in.
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("GraceLinkCrash", "Uncaught exception on ${thread.name}", throwable)
            } catch (_: Throwable) { /* never let logging itself throw */ }
            previous?.uncaughtException(thread, throwable)
        }

        // Firebase.initializeApp(this)  // enable after google-services.json is in place
        // WorkManager.initialize(...)   // enabled on-demand via Configuration.Provider
    }
}
