package com.gracelink.android.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.player.GracePlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val content: ContentEntity? = null,
    val player: GracePlayerController.PlayerUiState = GracePlayerController.PlayerUiState(),
    val isFavorite: Boolean = false,
    val sleepTimer: Int? = null,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val contentRepo: ContentRepository,
    private val playerController: GracePlayerController,
) : ViewModel() {

    private val content = MutableStateFlow<ContentEntity?>(null)
    private val sleepTimer = MutableStateFlow<Int?>(null)

    val state: StateFlow<PlayerState> = combine(
        content, playerController.state, contentRepo.favorites(), sleepTimer
    ) { c, p, favs, sleep ->
        PlayerState(c, p, c?.id in favs, sleep)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerState())

    fun load(contentId: String) = viewModelScope.launch {
        val c = contentRepo.getById(contentId) ?: return@launch
        content.value = c
        playerController.play(c)
        launch { playerController.pollPosition() }
    }

    fun togglePlayPause() = playerController.togglePlayPause()
    fun seekTo(ms: Long) = playerController.seekTo(ms)
    fun setSpeed(s: Float) = playerController.setSpeed(s)
    fun toggleFavorite() = viewModelScope.launch {
        val c = content.value ?: return@launch
        if (state.value.isFavorite) contentRepo.removeFavorite(c.id) else contentRepo.addFavorite(c.id)
    }
    fun setSleepTimer(m: Int?) {
        sleepTimer.value = m
        if (m != null) playerController.setSleepTimer(m, viewModelScope)
    }
}
