package com.gracelink.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.model.ContentLanguage
import com.gracelink.android.data.model.User
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileUiState(
    val user: User = User(
        uid = "",
        displayName = "Guest",
        email = "",
        preferredLanguage = ContentLanguage.EN,
    ),
    val favoritesCount: Int = 0,
    val downloadsCount: Int = 0,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userRepository: UserRepository,
    contentRepository: ContentRepository,
) : ViewModel() {

    val state: StateFlow<ProfileUiState> = combine(
        userRepository.user,
        contentRepository.favorites,
        contentRepository.downloads,
    ) { user, favs, downloads ->
        ProfileUiState(
            user = user,
            favoritesCount = favs.size,
            downloadsCount = downloads.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())
}
