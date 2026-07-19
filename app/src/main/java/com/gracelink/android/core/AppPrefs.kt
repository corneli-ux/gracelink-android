package com.gracelink.android.core

import android.content.Context

/**
 * Tiny local-only flag store for onboarding state.
 * Deliberately not tied to Hilt/DataStore — this is read once at nav-graph
 * start-up to decide Splash's next stop, nothing more.
 */
object AppPrefs {
    private const val PREFS_NAME = "gracelink_prefs"
    private const val KEY_HAS_ONBOARDED = "has_onboarded"

    fun hasOnboarded(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_ONBOARDED, false)

    fun setOnboarded(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_ONBOARDED, true)
            .apply()
    }
}
