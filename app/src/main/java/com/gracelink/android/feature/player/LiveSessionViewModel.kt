package com.gracelink.android.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.ChatMessageEntity
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.repository.LiveSessionRepository
import com.gracelink.android.data.repository.PrayerRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveSessionState(
    val session: LiveSessionEntity? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val isQuestionMode: Boolean = false,
    val myName: String = "You",
)

@HiltViewModel
class LiveSessionViewModel @Inject constructor(
    private val liveRepo: LiveSessionRepository,
    private val prayerRepo: PrayerRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val session = MutableStateFlow<LiveSessionEntity?>(null)
    private val isQuestionMode = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val messagesFlow = session.flatMapLatest { s ->
        if (s != null) prayerRepo.chatFor(s.id) else kotlinx.coroutines.flow.flowOf(emptyList())
    }

    val state: StateFlow<LiveSessionState> = combine(session, messagesFlow, isQuestionMode, userRepo.current()) { s, msgs, q, user ->
        LiveSessionState(s, msgs, q, user?.displayName ?: "You")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LiveSessionState())

    fun load(sessionId: String) = viewModelScope.launch {
        session.value = liveRepo.getById(sessionId)
    }

    fun toggleQuestionMode() { isQuestionMode.value = !isQuestionMode.value }

    fun sendMessage(text: String) = viewModelScope.launch {
        val sId = session.value?.id ?: return@launch
        prayerRepo.sendMessage(sId, state.value.myName, text, isQuestionMode.value)
    }
}
