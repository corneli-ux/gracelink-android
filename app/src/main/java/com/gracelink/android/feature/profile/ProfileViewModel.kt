package com.gracelink.android.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.UserEntity
import com.gracelink.android.data.repository.CloudProfileRegistry
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.DownloadRepository
import com.gracelink.android.data.repository.MediaUploadRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: UserEntity? = null,
    val favoritesCount: Int = 0,
    val downloadsCount: Int = 0,
    val isUploadingPhoto: Boolean = false,
    val photoUploadError: String? = null,
    val myChurchId: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val contentRepo: ContentRepository,
    private val downloadRepo: DownloadRepository,
    private val mediaUpload: MediaUploadRepository,
    private val churchDao: ChurchDao,
    private val cloudRegistry: CloudProfileRegistry,
) : ViewModel() {

    private val isUploadingPhoto = MutableStateFlow(false)
    private val photoUploadError = MutableStateFlow<String?>(null)

    private val baseState: StateFlow<ProfileState> = combine(
        userRepo.current(), contentRepo.favorites(), downloadRepo.all(), isUploadingPhoto, photoUploadError,
    ) { user, favs, downloads, uploading, error ->
        ProfileState(user, favs.size, downloads.size, uploading, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState())

    // A church account's own "CHURCH" badge should open their real public
    // profile page (same one anyone else sees) -- previously that badge
    // was a plain, non-interactive label with no way to reach it at all.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ProfileState> = baseState.flatMapLatest { base ->
        val uid = base.user?.uid
        if (uid == null || base.user.accountType != AccountType.CHURCH) {
            flowOf(base)
        } else {
            churchDao.byOwner(uid).map { church -> base.copy(myChurchId = church?.id) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState())

    fun setLanguage(lang: ContentLanguage) = viewModelScope.launch {
        state.value.user?.let { userRepo.setLanguage(it.uid, lang) }
    }
    fun setDataSaver(enabled: Boolean) = viewModelScope.launch {
        state.value.user?.let { userRepo.setDataSaver(it.uid, enabled) }
    }
    fun setNotifications(enabled: Boolean) = viewModelScope.launch {
        state.value.user?.let { userRepo.setNotifications(it.uid, enabled) }
    }
    fun signOut() = viewModelScope.launch { userRepo.signOut() }

    fun uploadProfilePhoto(uri: Uri) {
        val uid = state.value.user?.uid ?: return
        isUploadingPhoto.value = true
        photoUploadError.value = null
        viewModelScope.launch {
            try {
                val url = mediaUpload.uploadContentUri(uri, "profile_photos/$uid")
                userRepo.setPhotoUrl(uid, url)
                cloudRegistry.updatePhotoUrl(uid, url)
            } catch (e: Exception) {
                photoUploadError.value = "Couldn't upload photo: ${e.message}"
            } finally {
                isUploadingPhoto.value = false
            }
        }
    }
}
