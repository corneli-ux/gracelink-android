package com.gracelink.android.feature.churchportal

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.data.db.entity.ChurchEntity
import com.gracelink.android.data.repository.CloudProfileRegistry
import com.gracelink.android.data.repository.MediaUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChurchEditProfileState(
    val church: ChurchEntity? = null,
    val isUploadingPhoto: Boolean = false,
    val photoUploadError: String? = null,
)

@HiltViewModel
class ChurchEditProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val churchDao: ChurchDao,
    private val cloudRegistry: CloudProfileRegistry,
    private val mediaUpload: MediaUploadRepository,
) : ViewModel() {

    private val isUploadingPhoto = MutableStateFlow(false)
    private val photoUploadError = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val churchFlow: StateFlow<ChurchEntity?> = userDao.current().flatMapLatest { user ->
        user?.let { churchDao.byOwner(it.uid) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val state: StateFlow<ChurchEditProfileState> = combine(
        churchFlow, isUploadingPhoto, photoUploadError,
    ) { church, uploading, error ->
        ChurchEditProfileState(church, uploading, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChurchEditProfileState())

    fun save(
        name: String,
        description: String,
        pastorName: String,
        location: String,
        belief: BeliefSystem,
        website: String,
        phone: String,
        onDone: () -> Unit,
    ) = viewModelScope.launch {
        val current = state.value.church ?: return@launch
        churchDao.update(
            current.copy(
                name = name,
                description = description,
                pastorName = pastorName,
                location = location,
                beliefSystem = belief,
                website = website.ifBlank { null },
                phone = phone.ifBlank { null },
            )
        )
        // Keep the cloud restoration backup in sync with edits, so a
        // reinstall restores the CURRENT profile, not a stale snapshot
        // from whenever registration originally happened.
        current.ownerUserId?.let { uid ->
            cloudRegistry.writeChurch(
                uid = uid, churchName = name, pastorName = pastorName, location = location,
                beliefSystem = belief, description = description,
                website = website.ifBlank { null }, phone = phone.ifBlank { null },
            )
        }
        onDone()
    }

    fun uploadPhoto(uri: Uri) {
        val current = state.value.church ?: return
        isUploadingPhoto.value = true
        photoUploadError.value = null
        viewModelScope.launch {
            try {
                val url = mediaUpload.uploadContentUri(uri, "church_photos/${current.id}")
                churchDao.update(current.copy(photoUrl = url))
            } catch (e: Exception) {
                photoUploadError.value = "Couldn't upload photo: ${e.message}"
            } finally {
                isUploadingPhoto.value = false
            }
        }
    }
}
