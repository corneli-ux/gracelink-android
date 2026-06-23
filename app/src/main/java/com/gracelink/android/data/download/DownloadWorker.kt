package com.gracelink.android.data.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gracelink.android.data.db.GraceDatabase
import com.gracelink.android.data.db.entity.DownloadEntity
import kotlinx.coroutines.delay

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val contentId = inputData.getString("contentId") ?: return Result.failure()
            val title = inputData.getString("title") ?: ""
            val audioUrl = inputData.getString("audioUrl") ?: ""
            delay(2000)
            val db = GraceDatabase.getInstance(applicationContext)
            db.downloadDao().add(DownloadEntity(contentId, title, audioUrl, System.currentTimeMillis(), 25_000_000L))
            Result.success()
        } catch (e: Exception) { Result.failure() }
    }
}
