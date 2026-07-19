package com.gracelink.android.feature.podcast

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import com.gracelink.android.data.repository.MediaUploadRepository
import com.gracelink.android.data.repository.PodcastRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val uploadError: String? = null,
)

@HiltViewModel
class PodcastCreateViewModel @Inject constructor(
    private val repo: PodcastRepository,
    private val mediaUpload: MediaUploadRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val isUploading = MutableStateFlow(false)
    private val uploadError = MutableStateFlow<String?>(null)

    val state: StateFlow<PodcastCreateState> = combine(
        userRepo.current(), repo.allSeries(), isUploading, uploadError,
    ) { user, all, uploading, error ->
        PodcastCreateState(
            myName = user?.displayName ?: "",
            myUid = user?.uid ?: "",
            myAccountType = user?.accountType ?: AccountType.PERSONAL,
            mySeries = if (user != null) all.filter { it.authorId == user.uid } else emptyList(),
            isUploading = uploading,
            uploadError = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PodcastCreateState())

    fun createSeries(title: String, description: String, category: String, onDone: (String) -> Unit) = viewModelScope.launch {
        val s = state.value
        if (s.myUid.isBlank() || title.isBlank()) return@launch
        val id = repo.createSeries(
            authorId = s.myUid,
            authorName = s.myName,
            authorType = s.myAccountType,
            churchId = null,
            title = title,
            description = description,
            coverUrl = null,
            category = category,
        )
        onDone(id)
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
}
