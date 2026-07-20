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

data class GroupChatMessage(
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAt: Long = 0,
)

/**
 * Real, shared group chat backed by Firestore. Previously group messages
 * lived in Room only (GroupMessageEntity/GroupMessageDao) -- a message
 * one member sent was never visible on any other member's device, the
 * same class of bug already fixed for Prayer and Live Spaces. This
 * replaces that path entirely for the chat feature specifically (the
 * Room tables/entities are left in place, just no longer read from --
 * removing them would need another migration for zero benefit).
 */
@Singleton
class GroupChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("group_chat_messages")

    fun messagesFor(groupId: String): Flow<List<GroupChatMessage>> = callbackFlow {
        val registration = collection
            .whereEqualTo("groupId", groupId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<GroupChatMessage>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun send(groupId: String, senderId: String, senderName: String, text: String) {
        val id = "gmsg_${System.currentTimeMillis()}"
        val message = GroupChatMessage(
            id = id, groupId = groupId, senderId = senderId,
            senderName = senderName, text = text, createdAt = System.currentTimeMillis(),
        )
        collection.document(id).set(message).await()
    }
}
