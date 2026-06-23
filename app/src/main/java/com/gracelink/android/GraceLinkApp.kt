package com.gracelink.android

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GraceLink Application entry point.
 *
 * Hilt bootstraps the dependency graph here. We deliberately keep this lean —
 * any global one-time setup (analytics, crashlytics, work manager config)
 * should live in a dedicated initializer and be called from [onCreate].
 *
 * A default uncaught-exception handler is installed so any crash stack trace
 * is written BOTH to logcat (tag "GraceLinkCrash") AND to a file on disk
 * (`/sdcard/Download/gracelink-crash-<timestamp>.txt` or app cache dir).
 * This lets us diagnose crashes without `adb logcat` access — just pull the
 * file from the device's Files app.
 */
@HiltAndroidApp
class GraceLinkApp : Application() {
    override fun onCreate() {
        Log.d("GraceLinkLaunch", "GraceLinkApp.onCreate: START")
        super.onCreate()
        Log.d("GraceLinkLaunch", "GraceLinkApp.onCreate: after super (Hilt initialized)")

        // Crash logger — writes the full stack trace to logcat + a file
        // before the process dies. Replace with Crashlytics once Firebase is
        // wired in.
        try {
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    Log.e("GraceLinkCrash", "Uncaught exception on ${thread.name}", throwable)
                    writeCrashToFile(thread.name, throwable)
                } catch (_: Throwable) { /* never let logging itself throw */ }
                previous?.uncaughtException(thread, throwable)
            }
            Log.d("GraceLinkLaunch", "GraceLinkApp.onCreate: crash logger installed")
        } catch (e: Exception) {
            Log.e("GraceLinkLaunch", "Failed to install crash logger", e)
        }

        Log.d("GraceLinkLaunch", "GraceLinkApp.onCreate: DONE")
    }

    /**
     * Writes the crash to a plain-text file the user can find and share.
     * Tries Downloads first (most accessible), falls back to app cache.
     */
    private fun writeCrashToFile(threadName: String, throwable: Throwable) {
        val sw = StringWriter()
        sw.append("=== GraceLink Crash Report ===\n")
        sw.append("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())}\n")
        sw.append("Thread: $threadName\n")
        sw.append("App version: 1.0.0-mvp\n")
        sw.append("\nStack trace:\n")
        throwable.printStackTrace(PrintWriter(sw))
        sw.append("\n=== End of report ===\n")
        val report = sw.toString()

        try {
            // Try Downloads dir first (most accessible from a file manager)
            val downloadsDir = File("/sdcard/Download")
            if (downloadsDir.canWrite()) {
                val outFile = File(downloadsDir, "gracelink-crash-${System.currentTimeMillis()}.txt")
                outFile.writeText(report)
                Log.i("GraceLinkCrash", "Crash report written to: ${outFile.absolutePath}")
                return
            }
        } catch (_: Throwable) { }

        try {
            // Fallback: app cache dir
            val outFile = File(cacheDir, "gracelink-crash-${System.currentTimeMillis()}.txt")
            outFile.writeText(report)
            Log.i("GraceLinkCrash", "Crash report written to: ${outFile.absolutePath}")
        } catch (_: Throwable) { }
    }
}
