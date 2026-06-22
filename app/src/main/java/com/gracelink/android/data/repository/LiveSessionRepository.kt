package com.gracelink.android.data.repository

import com.gracelink.android.data.mock.MockData
import com.gracelink.android.data.model.ChatMessage
import com.gracelink.android.data.model.LiveSession
import com.gracelink.android.data.model.LiveSessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveSessionRepository @Inject constructor() {

    private val _sessions = MutableStateFlow(MockData.liveSessions)
    val sessions: StateFlow<List<LiveSession>> = _sessions.asStateFlow()

    private val _chat = MutableStateFlow(MockData.liveChatMessages)
    val chat: StateFlow<List<ChatMessage>> = _chat.asStateFlow()

    suspend fun fetchSessions(): List<LiveSession> = withContext(Dispatchers.IO) {
        delay(250)
        _sessions.value
    }

    fun toggleRemindMe(id: String) {
        _sessions.update { list ->
            list.map { if (it.id == id) it.copy(remindMe = !it.remindMe) else it }
        }
    }

    fun toggleJoinQueue(id: String) {
        _sessions.update { list ->
            list.map { if (it.id == id) it.copy(joinedQueue = !it.joinedQueue) else it }
        }
    }

    fun getById(id: String): LiveSession? = _sessions.value.firstOrNull { it.id == id }

    /** Send a chat message. Phase 2: route to Firestore. */
    fun sendMessage(sessionId: String, displayName: String, text: String, isQuestion: Boolean = false) {
        val msg = ChatMessage(
            id = "m_${System.currentTimeMillis()}",
            sessionId = sessionId,
            userId = "u_demo",
            displayName = displayName,
            text = text,
            timestamp = System.currentTimeMillis(),
            isQuestion = isQuestion,
            isMine = true,
        )
        _chat.update { it + msg }
    }

    fun getByStatus(status: LiveSessionStatus): List<LiveSession> =
        _sessions.value.filter { it.status == status }
}
