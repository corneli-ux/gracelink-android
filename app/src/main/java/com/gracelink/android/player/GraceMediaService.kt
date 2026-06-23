package com.gracelink.android.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.gracelink.android.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Background-playback service.
 *
 * Exposes the singleton ExoPlayer (owned by [GracePlayerController]) to the
 * system via MediaSession, enabling the media notification + Bluetooth /
 * Android Auto controls.
 *
 * IMPORTANT: We do NOT release the player in onDestroy — it's a Hilt
 * @Singleton owned by the app, not the service. Releasing it here would
 * kill playback for the entire app when the service stops.
 */
@AndroidEntryPoint
class GraceMediaService : MediaSessionService() {

    @Inject lateinit var playerController: GracePlayerController

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val sessionIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, sessionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        mediaSession = MediaSession.Builder(this, playerController.player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        // Release only the MediaSession — NOT the player.
        // The player is a Hilt @Singleton owned by GracePlayerController.
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
