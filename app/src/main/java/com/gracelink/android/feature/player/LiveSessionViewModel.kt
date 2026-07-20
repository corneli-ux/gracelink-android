package com.gracelink.android.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.repository.LiveChatMessage
import com.gracelink.android.data.repository.LiveSessionChatRepository
import com.gracelink.android.data.repository.LiveSessionRepository
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
    val messages: List<LiveChatMessage> = emptyList(),
    val isQuestionMode: Boolean = false,
    val myUid: String = "",
    val myName: String = "You",
)

@HiltViewModel
class LiveSessionViewModel @Inject constructor(
    private val liveRepo: LiveSessionRepository,
    private val chatRepo: LiveSessionChatRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val session = MutableStateFlow<LiveSessionEntity?>(null)
    private val isQuestionMode = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val messagesFlow = session.flatMapLatest { s ->
        if (s != null) chatRepo.messagesFor(s.id) else kotlinx.coroutines.flow.flowOf(emptyList())
    }

    val state: StateFlow<LiveSessionState> = combine(session, messagesFlow, isQuestionMode, userRepo.current()) { s, msgs, q, user ->
        LiveSessionState(s, msgs, q, user?.uid ?: "", user?.displayName ?: "You")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LiveSessionState())

    fun load(sessionId: String) = viewModelScope.launch {
        session.value = liveRepo.getById(sessionId)
    }

    fun toggleQuestionMode() { isQuestionMode.value = !isQuestionMode.value }

    fun sendMessage(text: String) = viewModelScope.launch {
        val s = state.value
        val sId = s.session?.id ?: return@launch
        if (s.myUid.isBlank() || text.isBlank()) return@launch
        chatRepo.send(sId, s.myUid, s.myName, text, isQuestionMode.value)
    }
}
