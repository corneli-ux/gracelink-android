package com.gracelink.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Reactions in the context of Scripture, not generic social-media emoji. */
enum class BiblicalReaction(val label: String, val emoji: String) {
    AMEN("Amen", "\uD83D\uDE4F"),
    HALLELUJAH("Hallelujah", "\u2728"),
    PRAYING("Praying", "\uD83D\uDD4A\uFE0F"),
    BLESSED("Blessed", "\u2764\uFE0F"),
    GLORY("Glory to God", "\uD83D\uDC51"),
}

data class ReactionSummary(
    val counts: Map<BiblicalReaction, Int> = emptyMap(),
    val myReaction: BiblicalReaction? = null,
) {
    val total: Int get() = counts.values.sum()
}

private data class ReactionDoc(
    val id: String = "",
    val contentType: String = "",
    val contentId: String = "",
    val userId: String = "",
    val reaction: String = "",
)

/**
 * Reactions are keyed by (contentType, contentId) so the same reaction bar
 * works on any Timeline item -- articles, podcasts, prayers, events,
 * forum questions -- without needing a separate table per content type.
 */
@Singleton
class ReactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("reactions")

    private fun docId(contentType: String, contentId: String, userId: String) = "${contentType}_${contentId}_$userId"

    fun reactionsFor(contentType: String, contentId: String, myUid: String): Flow<ReactionSummary> = callbackFlow {
        val registration = collection
            .whereEqualTo("contentType", contentType)
            .whereEqualTo("contentId", contentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ReactionSummary())
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents?.mapNotNull { it.toObject<ReactionDoc>() } ?: emptyList()
                val counts = docs.groupingBy { it.reaction }.eachCount()
                    .mapNotNull { (name, count) -> runCatching { BiblicalReaction.valueOf(name) }.getOrNull()?.let { it to count } }
                    .toMap()
                val mine = docs.firstOrNull { it.userId == myUid }?.reaction?.let { runCatching { BiblicalReaction.valueOf(it) }.getOrNull() }
                trySend(ReactionSummary(counts, mine))
            }
        awaitClose { registration.remove() }
    }

    /** Tapping the same reaction again removes it; tapping a different one switches it. */
    suspend fun react(contentType: String, contentId: String, userId: String, reaction: BiblicalReaction) {
        val id = docId(contentType, contentId, userId)
        collection.document(id).set(
            ReactionDoc(id = id, contentType = contentType, contentId = contentId, userId = userId, reaction = reaction.name)
        ).await()
    }

    suspend fun removeReaction(contentType: String, contentId: String, userId: String) {
        collection.document(docId(contentType, contentId, userId)).delete().await()
    }
}
