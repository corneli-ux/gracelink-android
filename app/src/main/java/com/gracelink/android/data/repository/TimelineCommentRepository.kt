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

data class TimelineComment(
    val id: String = "",
    val contentType: String = "",
    val contentId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long = 0,
)

/**
 * One comment thread implementation shared by every Timeline item type
 * (article, podcast, prayer, event, forum question) via (contentType,
 * contentId) -- rather than five separate per-type comment systems.
 */
@Singleton
class TimelineCommentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("timeline_comments")

    fun commentsFor(contentType: String, contentId: String): Flow<List<TimelineComment>> = callbackFlow {
        val registration = collection
            .whereEqualTo("contentType", contentType)
            .whereEqualTo("contentId", contentId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc -> doc.toObject<TimelineComment>()?.copy(id = doc.id) } ?: emptyList()
                trySend(comments)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addComment(contentType: String, contentId: String, authorId: String, authorName: String, text: String) {
        val id = "tc_${System.currentTimeMillis()}"
        val comment = TimelineComment(
            id = id, contentType = contentType, contentId = contentId,
            authorId = authorId, authorName = authorName, text = text, createdAt = System.currentTimeMillis(),
        )
        collection.document(id).set(comment).await()
    }
}
