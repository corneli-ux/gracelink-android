package com.gracelink.android.feature.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.download.DownloadWorker
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.DownloadRepository
import com.gracelink.android.player.GracePlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val isDownloaded: Boolean = false,
    val sleepTimer: Int? = null,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentRepo: ContentRepository,
    private val downloadRepo: DownloadRepository,
    private val playerController: GracePlayerController,
) : ViewModel() {

    private val content = MutableStateFlow<ContentEntity?>(null)
    private val sleepTimer = MutableStateFlow<Int?>(null)
    private val isDownloaded = MutableStateFlow(false)

    val state: StateFlow<PlayerState> = combine(
        content, playerController.state, contentRepo.favorites(), sleepTimer, isDownloaded
    ) { c, p, favs, sleep, dl ->
        PlayerState(c, p, c?.id in favs, dl, sleep)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerState())

    fun load(contentId: String) = viewModelScope.launch {
        val c = contentRepo.getById(contentId) ?: return@launch
        content.value = c
        playerController.play(c)
        launch { playerController.pollPosition() }
        launch { downloadRepo.isDownloaded(contentId).collect { isDownloaded.value = it } }
    }

    fun togglePlayPause() = playerController.togglePlayPause()
    fun seekTo(ms: Long) = playerController.seekTo(ms)
    fun setSpeed(s: Float) = playerController.setSpeed(s)
    fun toggleFavorite() = viewModelScope.launch {
        val c = content.value ?: return@launch
        contentRepo.toggleFavorite(c.id)
    }
    fun setSleepTimer(m: Int?) {
        sleepTimer.value = m
        if (m != null) playerController.setSleepTimer(m, viewModelScope)
    }

    fun download() = viewModelScope.launch {
        val c = content.value ?: return@launch
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf("contentId" to c.id, "title" to c.title, "audioUrl" to c.audioUrl))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
