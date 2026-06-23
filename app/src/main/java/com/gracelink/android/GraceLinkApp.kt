package com.gracelink.android

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.player.GraceMessagingService
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.work.HiltWorkerFactory
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
class GraceLinkApp : Application(), Configuration.Provider {

    @Inject lateinit var contentRepository: ContentRepository
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()

        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try { Log.e("GraceLinkCrash", "Uncaught on ${thread.name}", throwable); writeCrash(thread.name, throwable) } catch (_: Throwable) {}
            previous?.uncaughtException(thread, throwable)
        }

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try { Firebase.auth.signInAnonymously().await(); contentRepository.syncFromFirestore() } catch (e: Exception) { Log.e("GraceLink", "Auth/sync failed", e) }
        }

        GraceMessagingService.createChannels(this)
        GraceMessagingService.subscribeToTopics()
    }

    private fun writeCrash(thread: String, t: Throwable) {
        val sw = StringWriter()
        sw.append("=== GraceLink Crash ===\nTime: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())}\nThread: $thread\n\n")
        t.printStackTrace(PrintWriter(sw))
        try { val dir = File(getExternalFilesDir(null), "crashes"); if (!dir.exists()) dir.mkdirs(); File(dir, "crash-${System.currentTimeMillis()}.txt").writeText(sw.toString()) } catch (_: Throwable) {}
    }
}
