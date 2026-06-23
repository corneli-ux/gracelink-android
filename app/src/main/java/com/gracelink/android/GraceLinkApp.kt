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

@HiltAndroidApp
class GraceLinkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("GraceLinkCrash", "Uncaught on ${thread.name}", throwable)
                writeCrash(thread.name, throwable)
            } catch (_: Throwable) {}
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrash(thread: String, t: Throwable) {
        val sw = StringWriter()
        sw.append("=== GraceLink Crash ===\n")
        sw.append("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())}\n")
        sw.append("Thread: $thread\n\n")
        t.printStackTrace(PrintWriter(sw))
        try {
            val dir = File(getExternalFilesDir(null), "crashes")
            if (!dir.exists()) dir.mkdirs()
            File(dir, "crash-${System.currentTimeMillis()}.txt").writeText(sw.toString())
        } catch (_: Throwable) {}
    }
}
