package com.gracelink.android.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.db.entity.LiveSessionStatus
import com.gracelink.android.data.db.entity.UserEntity
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.LiveSessionRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val greeting: String = "",
    val userName: String = "",
    val liveRadio: List<ContentEntity> = emptyList(),
    val liveSession: LiveSessionEntity? = null,
    val continueListening: List<ContentEntity> = emptyList(),
    val recommended: List<ContentEntity> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepo: ContentRepository,
    private val liveRepo: LiveSessionRepository,
    userRepo: UserRepository,
) : ViewModel() {

    val state: StateFlow<HomeState> = combine(
        contentRepo.liveRadio(),
        contentRepo.library(),
        liveRepo.byStatus(LiveSessionStatus.LIVE),
        userRepo.current(),
    ) { liveRadio, library, liveSessions, user ->
        HomeState(
            greeting = greetingFor(),
            userName = user?.displayName ?: "",
            liveRadio = liveRadio,
            liveSession = liveSessions.firstOrNull(),
            continueListening = library.take(3),
            recommended = library.shuffled().take(6),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    private fun greetingFor(): String {
        val hour = java.time.LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Peace be with you"
        }
    }
}
