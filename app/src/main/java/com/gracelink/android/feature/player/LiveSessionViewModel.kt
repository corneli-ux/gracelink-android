package com.gracelink.android.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.model.ChatMessage
import com.gracelink.android.data.model.LiveSession
import com.gracelink.android.data.repository.LiveSessionRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveSessionUiState(
    val session: LiveSession? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isQuestionMode: Boolean = false,
    val myDisplayName: String = "You",
)

@HiltViewModel
class LiveSessionViewModel @Inject constructor(
    private val liveRepository: LiveSessionRepository,
    userRepository: UserRepository,
) : ViewModel() {

    private val _session = MutableStateFlow<LiveSession?>(null)
    private val _isQuestionMode = MutableStateFlow(false)

    val state: StateFlow<LiveSessionUiState> = combine(
        _session,
        liveRepository.chat,
        _isQuestionMode,
        userRepository.user,
    ) { session, messages, qMode, user ->
        LiveSessionUiState(
            session = session,
            messages = messages.filter { it.sessionId == session?.id },
            isQuestionMode = qMode,
            myDisplayName = user.displayName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LiveSessionUiState())

    fun load(sessionId: String) {
        _session.value = liveRepository.getById(sessionId)
    }

    fun toggleQuestionMode() { _isQuestionMode.value = !_isQuestionMode.value }

    fun sendMessage(text: String) {
        val sessionId = _session.value?.id ?: return
        liveRepository.sendMessage(
            sessionId = sessionId,
            displayName = state.value.myDisplayName,
            text = text,
            isQuestion = _isQuestionMode.value,
        )
    }
}
