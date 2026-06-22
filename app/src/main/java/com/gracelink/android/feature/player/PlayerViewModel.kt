package com.gracelink.android.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.model.ContentItem
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.LiveSessionRepository
import com.gracelink.android.player.GracePlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val content: ContentItem? = null,
    val playerState: GracePlayerController.PlayerUiState = GracePlayerController.PlayerUiState(),
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val sleepTimerMinutes: Int? = null,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val liveRepository: LiveSessionRepository,
    private val playerController: GracePlayerController,
) : ViewModel() {

    private val _content = MutableStateFlow<ContentItem?>(null)
    private val _sleepTimer = MutableStateFlow<Int?>(null)

    val state: StateFlow<PlayerUiState> = combine(
        _content,
        playerController.state,
        contentRepository.favorites,
        contentRepository.downloads,
        _sleepTimer,
    ) { content, pState, favs, downloads, sleep ->
        PlayerUiState(
            content = content,
            playerState = pState,
            isFavorite = content?.id in favs,
            isDownloaded = content?.id in downloads,
            sleepTimerMinutes = sleep,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUiState())

    fun load(contentId: String) {
        viewModelScope.launch {
            val content = contentRepository.getById(contentId) ?: return@launch
            _content.value = content
            playerController.play(content)
            // Start polling for current position
            viewModelScope.launch { playerController.pollPosition() }
        }
    }

    fun togglePlayPause() = playerController.togglePlayPause()
    fun seekTo(ms: Long) = playerController.seekTo(ms)
    fun setSpeed(speed: Float) = playerController.setSpeed(speed)
    fun toggleFavorite() = _content.value?.let { contentRepository.toggleFavorite(it.id) }
    fun toggleDownload() = _content.value?.let { contentRepository.toggleDownload(it.id) }

    fun setSleepTimer(minutes: Int?) {
        _sleepTimer.value = minutes
        if (minutes != null) playerController.setSleepTimerMinutes(minutes, viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        // Don't release the player if we want background playback —
        // the MediaSessionService will own the player in Phase 1.5.
        // For MVP, releasing here would stop audio when the screen dies.
    }
}
