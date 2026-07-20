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

data class DirectMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val text: String = "",
    val createdAt: Long = 0,
)

/**
 * One-to-one direct messaging, built Firestore-backed from the start
 * (unlike group/live-session chat, which started Room-local and needed
 * a follow-up fix) -- real-time shared across both participants' devices.
 */
@Singleton
class DirectMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("direct_messages")

    private fun conversationId(uidA: String, uidB: String) = listOf(uidA, uidB).sorted().joinToString("_")

    fun conversation(uidA: String, uidB: String): Flow<List<DirectMessage>> = callbackFlow {
        val convId = conversationId(uidA, uidB)
        val registration = collection
            .whereEqualTo("conversationId", convId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<DirectMessage>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun send(senderId: String, senderName: String, receiverId: String, text: String) {
        val id = "dm_${System.currentTimeMillis()}"
        val message = DirectMessage(
            id = id, conversationId = conversationId(senderId, receiverId),
            senderId = senderId, senderName = senderName, receiverId = receiverId,
            text = text, createdAt = System.currentTimeMillis(),
        )
        collection.document(id).set(message).await()
    }
}
