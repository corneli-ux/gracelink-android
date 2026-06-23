package com.gracelink.android

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.player.GraceMessagingService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class GraceLinkApp : Application() {

    @Inject lateinit var contentRepository: ContentRepository

    override fun onCreate() {
        super.onCreate()

        // Install crash logger
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("GraceLinkCrash", "Uncaught on ${thread.name}", throwable)
                writeCrash(thread.name, throwable)
            } catch (_: Throwable) {}
            previous?.uncaughtException(thread, throwable)
        }

        // Sign in anonymously + sync content from Firestore
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                Firebase.auth.signInAnonymously().await()
                Log.d("GraceLink", "Anonymous auth success")
                contentRepository.syncFromFirestore()
                Log.d("GraceLink", "Firestore sync complete")
            } catch (e: Exception) {
                Log.e("GraceLink", "Auth/sync failed", e)
            }
        }

        // Create notification channels + subscribe to FCM topics
        GraceMessagingService.createChannels(this)
        GraceMessagingService.subscribeToTopics()
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
