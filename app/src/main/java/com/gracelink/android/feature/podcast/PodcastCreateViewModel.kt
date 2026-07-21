package com.gracelink.android.feature.podcast

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import com.gracelink.android.data.repository.MediaUploadRepository
import com.gracelink.android.data.repository.PodcastRepository
import com.gracelink.android.data.repository.UserRepository
import com.gracelink.android.player.VoiceRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PodcastCreateState(
    val myName: String = "",
    val myUid: String = "",
    val myAccountType: AccountType = AccountType.PERSONAL,
    val mySeries: List<PodcastSeriesEntity> = emptyList(),
    val isUploading: Boolean = false,
    val isRecording: Boolean = false,
    val uploadError: String? = null,
)

@HiltViewModel
class PodcastCreateViewModel @Inject constructor(
    private val repo: PodcastRepository,
    private val mediaUpload: MediaUploadRepository,
    private val userRepo: UserRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val isUploading = MutableStateFlow(false)
    private val isRecording = MutableStateFlow(false)
    private val uploadError = MutableStateFlow<String?>(null)
    private var activeRecorder: VoiceRecorder? = null

    val state: StateFlow<PodcastCreateState> = combine(
        userRepo.current(), repo.allSeries(), isUploading, uploadError, isRecording,
    ) { user, all, uploading, error, recording ->
        PodcastCreateState(
            myName = user?.displayName ?: "",
            myUid = user?.uid ?: "",
            myAccountType = user?.accountType ?: AccountType.PERSONAL,
            mySeries = if (user != null) all.filter { it.authorId == user.uid } else emptyList(),
            isUploading = uploading,
            isRecording = recording,
            uploadError = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PodcastCreateState())

    /**
     * Resolves the author via a direct one-shot suspend query rather than
     * trusting state.value, which could still be the default (blank uid)
     * if this is tapped before the screen's first real state emission --
     * this screen does collect state, unlike Announcements/Groups/Forum's
     * create screens, but a fast tap right as the screen opens could still
     * race the async user/church resolution and silently no-op.
     */
    fun createSeries(title: String, description: String, category: String, coverUri: Uri?, onDone: (String) -> Unit) = viewModelScope.launch {
        val user = userRepo.currentOnce() ?: return@launch
        if (title.isBlank()) return@launch
        isUploading.value = coverUri != null
        uploadError.value = null
        try {
            val coverUrl = coverUri?.let { uri ->
                mediaUpload.uploadContentUri(uri, "podcasts/covers/${System.currentTimeMillis()}_${title.take(20).replace(" ", "_")}")
            }
            val id = repo.createSeries(
                authorId = user.uid,
                authorName = user.displayName,
                authorType = user.accountType,
                churchId = null,
                title = title,
                description = description,
                coverUrl = coverUrl,
                category = category,
            )
            onDone(id)
        } catch (e: Exception) {
            uploadError.value = "Couldn't upload cover image: ${e.message}"
        } finally {
            isUploading.value = false
        }
    }

    fun addEpisode(podcastId: String, title: String, audioUrl: String, durationLabel: String, onDone: () -> Unit) = viewModelScope.launch {
        if (title.isBlank() || audioUrl.isBlank()) return@launch
        repo.addEpisode(podcastId, title, audioUrl, durationLabel)
        onDone()
    }

    /** Uploads the picked audio file to Firebase Storage, then creates the episode with the resulting URL. */
    fun addEpisodeFromFile(podcastId: String, title: String, fileUri: Uri, durationLabel: String, onDone: () -> Unit) {
        if (title.isBlank()) return
        isUploading.value = true
        uploadError.value = null
        viewModelScope.launch {
            try {
                val path = "podcasts/$podcastId/${System.currentTimeMillis()}_${title.take(20).replace(" ", "_")}"
                val url = mediaUpload.uploadContentUri(fileUri, path)
                repo.addEpisode(podcastId, title, url, durationLabel)
                onDone()
            } catch (e: Exception) {
                uploadError.value = e.message ?: "Upload failed"
            } finally {
                isUploading.value = false
            }
        }
    }

    /** Record an episode directly in-app instead of picking an existing
     * file -- uses VOICE_COMMUNICATION as the recording source, which
     * routes through the device's built-in echo/noise/gain processing
     * on devices that support it (see VoiceRecorder for why this is the
     * source setting rather than attaching audiofx effects directly). */
    fun startRecording() {
        if (isRecording.value) return
        try {
            val recorder = VoiceRecorder(context)
            recorder.start()
            activeRecorder = recorder
            isRecording.value = true
        } catch (_: Exception) {
            activeRecorder = null
            isRecording.value = false
            uploadError.value = "Couldn't start recording -- check microphone permission."
        }
    }

    fun cancelRecording() {
        activeRecorder?.cancel()
        activeRecorder = null
        isRecording.value = false
    }

    fun stopRecordingAndUpload(podcastId: String, title: String, durationLabel: String, onDone: () -> Unit) {
        val recorder = activeRecorder ?: return
        val path = recorder.stop()
        activeRecorder = null
        isRecording.value = false
        if (path == null || title.isBlank()) return

        isUploading.value = true
        uploadError.value = null
        viewModelScope.launch {
            try {
                val storagePath = "podcasts/$podcastId/${System.currentTimeMillis()}_${title.take(20).replace(" ", "_")}"
                val url = mediaUpload.uploadLocalFile(path, storagePath)
                repo.addEpisode(podcastId, title, url, durationLabel)
                onDone()
            } catch (e: Exception) {
                uploadError.value = e.message ?: "Upload failed"
            } finally {
                isUploading.value = false
            }
        }
    }
}
