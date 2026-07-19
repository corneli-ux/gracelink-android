package com.gracelink.android.feature.podcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import com.gracelink.android.data.repository.PodcastRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
)

@HiltViewModel
class PodcastCreateViewModel @Inject constructor(
    private val repo: PodcastRepository,
    userRepo: UserRepository,
) : ViewModel() {

    val state: StateFlow<PodcastCreateState> = userRepo.current().let { userFlow ->
        combine(userFlow, repo.allSeries()) { user, all ->
            PodcastCreateState(
                myName = user?.displayName ?: "",
                myUid = user?.uid ?: "",
                myAccountType = user?.accountType ?: AccountType.PERSONAL,
                mySeries = if (user != null) all.filter { it.authorId == user.uid } else emptyList(),
            )
        }
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
}
