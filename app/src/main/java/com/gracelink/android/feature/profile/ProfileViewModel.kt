package com.gracelink.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.UserEntity
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.DownloadRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: UserEntity? = null,
    val favoritesCount: Int = 0,
    val downloadsCount: Int = 0,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val contentRepo: ContentRepository,
    private val downloadRepo: DownloadRepository,
) : ViewModel() {

    val state: StateFlow<ProfileState> = combine(
        userRepo.current(), contentRepo.favorites(), downloadRepo.all()
    ) { user, favs, downloads ->
        ProfileState(user, favs.size, downloads.size)
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
}
