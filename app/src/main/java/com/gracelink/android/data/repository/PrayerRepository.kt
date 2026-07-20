package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.ChatDao
import com.gracelink.android.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Despite the name (kept to avoid touching every injection site), this
 * class now only handles live-session chat -- the prayer-specific methods
 * that used to live here were fully replaced by PrayerFirestoreRepository,
 * which fixed two real bugs: every prayer author was hardcoded to the
 * literal "u_demo" instead of the real signed-in user, and prayers were
 * Room-only (never actually visible on anyone else's device at all).
 *
 * KNOWN GAP, not fixed this pass: sendMessage below has the exact same
 * u_demo/isMine hardcoding bug, AND chat messages are still Room-local
 * only -- a message one participant sends during a live session is never
 * seen by anyone else's device. Fixing this properly needs the same
 * Firestore-realtime treatment as Live Spaces and Prayer got, which is
 * its own separate chunk of work I flagged rather than rushed.
 */
@Singleton
class PrayerRepository @Inject constructor(
    private val chatDao: ChatDao,
) {
    fun chatFor(sessionId: String): Flow<List<ChatMessageEntity>> = chatDao.forSession(sessionId)

    suspend fun sendMessage(sessionId: String, displayName: String, text: String, isQuestion: Boolean) {
        chatDao.insert(
            ChatMessageEntity(
                id = "m_${System.currentTimeMillis()}",
                sessionId = sessionId,
                userId = "u_demo",
                displayName = displayName,
                text = text,
                timestamp = System.currentTimeMillis(),
                isModerator = false,
                isHost = false,
                isQuestion = isQuestion,
                isMine = true,
            )
        )
    }
}
