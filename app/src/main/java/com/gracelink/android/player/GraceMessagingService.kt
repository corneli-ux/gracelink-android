package com.gracelink.android.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gracelink.android.MainActivity
import com.gracelink.android.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Firebase Cloud Messaging service.
 *
 * Handles push notifications for:
 *  - Live event reminders ("Live Q&A starts in 15 minutes")
 *  - New content alerts ("New sermon from Pastor Anil")
 *  - Community updates ("Someone prayed for your request")
 *
 * The app subscribes to these topics on launch:
 *  - "live_events" — all live session reminders
 *  - "new_content" — new sermons/podcasts added
 *  - "prayer_updates" — encouragement on your prayers
 */
@AndroidEntryPoint
class GraceMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_LIVE = "live_events"
        const val CHANNEL_CONTENT = "new_content"
        const val CHANNEL_COMMUNITY = "community"

        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = context.getSystemService(NotificationManager::class.java)
                mgr.createNotificationChannel(NotificationChannel(CHANNEL_LIVE, "Live Events", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Reminders for live Q&A, debates, and worship nights"
                })
                mgr.createNotificationChannel(NotificationChannel(CHANNEL_CONTENT, "New Content", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "New sermons, podcasts, and music added"
                })
                mgr.createNotificationChannel(NotificationChannel(CHANNEL_COMMUNITY, "Community", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Prayer updates and encouragements"
                })
            }
        }

        fun subscribeToTopics() {
            val messaging = com.google.firebase.messaging.FirebaseMessaging.getInstance()
            messaging.subscribeToTopic("live_events")
            messaging.subscribeToTopic("new_content")
            messaging.subscribeToTopic("prayer_updates")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to server for user-specific notifications
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "GraceLink"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: "content"

        val channelId = when (type) {
            "live" -> CHANNEL_LIVE
            "community" -> CHANNEL_COMMUNITY
            else -> CHANNEL_CONTENT
        }

        showNotification(title, body, channelId)
    }

    private fun showNotification(title: String, body: String, channelId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val mgr = getSystemService(NotificationManager::class.java)
        mgr.notify(System.currentTimeMillis().toInt(), notification)
    }
}
