package com.gracelink.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * GraceLink Application entry point.
 *
 * Hilt bootstraps the dependency graph here. We deliberately keep this lean —
 * any global one-time setup (analytics, crashlytics, work manager config)
 * should live in a dedicated initializer and be called from [onCreate].
 */
@HiltAndroidApp
class GraceLinkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase.initializeApp(this)  // enable after google-services.json is in place
        // WorkManager.initialize(...)   // enabled on-demand via Configuration.Provider
    }
}
