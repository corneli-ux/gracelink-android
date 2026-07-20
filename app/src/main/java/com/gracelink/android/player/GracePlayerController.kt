package com.gracelink.android.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.gracelink.android.data.db.entity.ContentEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper around Media3 ExoPlayer.
 *
 * For the MVP we use ExoPlayer directly (no MediaController) — the
 * [GraceMediaService] owns the same player instance via Hilt @Singleton.
 * When the app is in the foreground we control playback directly; when it
 * goes to the background, the service's MediaSession keeps the player alive
 * and shows the system media notification.
 */
@Singleton
class GracePlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    data class PlayerUiState(
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val currentPositionMs: Long = 0L,
        val durationMs: Long = 0L,
        val playbackSpeed: Float = 1.0f,
        val current: ContentEntity? = null,
        val errorMessage: String? = null,
    )

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsLoadingChanged(isLoading: Boolean) {
            _state.update { it.copy(isLoading = isLoading) }
        }
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _state.update { it.copy(durationMs = player.duration.coerceAtLeast(0L)) }
            }
        }
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _state.update { it.copy(errorMessage = error.localizedMessage ?: "Playback error") }
        }
    }

    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { it.addListener(listener) }
    }

    private var sleepTimerJob: Job? = null

    fun play(content: ContentEntity) {
        val media = MediaItem.Builder()
            .setUri(content.audioUrl)
            .setMediaId(content.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(content.title)
                    .setArtist(content.speaker ?: "Faith Link")
                    .setArtworkUri(content.thumbnailUrl?.let { android.net.Uri.parse(it) })
                    .setMediaType(
                        if (content.type == com.gracelink.android.data.db.entity.ContentType.LIVE_RADIO)
                            MediaMetadata.MEDIA_TYPE_RADIO_STATION
                        else MediaMetadata.MEDIA_TYPE_PODCAST_EPISODE
                    )
                    .build()
            )
            .build()

        player.setMediaItem(media)
        player.prepare()
        player.playWhenReady = true
        _state.update { it.copy(current = content) }
    }

    fun togglePlayPause() { player.playWhenReady = !player.playWhenReady }
    fun seekTo(positionMs: Long) { player.seekTo(positionMs) }
    fun setSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
        _state.update { it.copy(playbackSpeed = speed) }
    }
    fun stop() {
        player.stop()
        _state.update { it.copy(current = null, isPlaying = false, currentPositionMs = 0L, durationMs = 0L) }
    }

    suspend fun pollPosition() {
        while (true) {
            val pos = if (player.duration > 0) player.currentPosition else 0L
            _state.update { it.copy(currentPositionMs = pos) }
            delay(500)
        }
    }

    fun setSleepTimer(minutes: Int, scope: CoroutineScope) {
        sleepTimerJob?.cancel()
        sleepTimerJob = scope.launch {
            delay(minutes * 60_000L)
            player.pause()
        }
    }

    fun release() {
        sleepTimerJob?.cancel()
        player.removeListener(listener)
        player.release()
    }
}
