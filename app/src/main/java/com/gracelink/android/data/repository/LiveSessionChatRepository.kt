package com.gracelink.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class LiveChatMessage(
    val id: String = "",
    val sessionId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val isHost: Boolean = false,
    val isModerator: Boolean = false,
    val isQuestion: Boolean = false,
)

/**
 * Real, shared live-session chat backed by Firestore. The previous
 * implementation had two real bugs: every message's author was
 * hardcoded to the literal "u_demo" instead of whoever actually sent
 * it, and "isMine" was a stored field always set to true -- so every
 * participant's device would have shown every message as their own.
 * Underneath both of those: chat was Room-local only, so a message one
 * participant sent was never visible on anyone else's device at all.
 * Same class of fix already applied to Prayer, Live Spaces, and Group
 * Chat -- "isMine" is now computed client-side by comparing userId to
 * the viewer's own uid, never stored.
 */
@Singleton
class LiveSessionChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("live_session_messages")

    fun messagesFor(sessionId: String): Flow<List<LiveChatMessage>> = callbackFlow {
        val registration = collection
            .whereEqualTo("sessionId", sessionId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<LiveChatMessage>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun send(
        sessionId: String,
        userId: String,
        displayName: String,
        text: String,
        isQuestion: Boolean,
        isHost: Boolean = false,
        isModerator: Boolean = false,
    ) {
        val id = "lmsg_${System.currentTimeMillis()}"
        val message = LiveChatMessage(
            id = id, sessionId = sessionId, userId = userId, displayName = displayName,
            text = text, timestamp = System.currentTimeMillis(),
            isHost = isHost, isModerator = isModerator, isQuestion = isQuestion,
        )
        collection.document(id).set(message).await()
    }
}
