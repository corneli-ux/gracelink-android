package com.gracelink.android.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class PrayerEncouragement(
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val audioUrl: String? = null,
    val timestamp: Long = 0,
)

data class PrayerRequest(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val isAnonymous: Boolean = false,
    val text: String = "",
    val timestamp: Long = 0,
    val isAnswered: Boolean = false,
    val prayedByUids: List<String> = emptyList(),
    val encouragements: List<PrayerEncouragement> = emptyList(),
)

/**
 * Real, multi-user prayer requests backed by Firestore. Previously this
 * was Room-only (local SQLite): a prayer one person submitted was never
 * visible on anyone else's device at all -- the entire "community prays
 * for you" concept was fundamentally single-player pretending to be
 * shared. On top of that, every prayer's author was hardcoded to the
 * literal string "u_demo" instead of the real signed-in user, and
 * "isMine" was a stored field that was always true for everyone -- so
 * "My Prayers" would have shown every prayer to every viewer.
 *
 * Fixed: real-time Firestore listener so every device sees the same live
 * list, real authorId from whoever is actually signed in, and "mine" /
 * "have I prayed this" computed per-viewer against prayedByUids rather
 * than stored as a single shared flag.
 */
@Singleton
class PrayerFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("prayer_requests")

    fun allPrayers(): Flow<List<PrayerRequest>> = callbackFlow {
        val registration = collection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val prayers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<PrayerRequest>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(prayers)
            }
        awaitClose { registration.remove() }
    }

    suspend fun submit(authorId: String, authorName: String, text: String, anonymous: Boolean) {
        val id = "pr_${System.currentTimeMillis()}"
        val prayer = PrayerRequest(
            id = id,
            authorId = authorId,
            authorName = if (anonymous) "Anonymous" else authorName,
            isAnonymous = anonymous,
            text = text,
            timestamp = System.currentTimeMillis(),
            isAnswered = false,
            prayedByUids = emptyList(),
            encouragements = emptyList(),
        )
        collection.document(id).set(prayer).await()
    }

    suspend fun togglePrayed(prayerId: String, uid: String, currentlyPrayed: Boolean) {
        collection.document(prayerId).update(
            "prayedByUids",
            if (currentlyPrayed) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid)
        ).await()
    }

    suspend fun markAnswered(prayerId: String) {
        collection.document(prayerId).update("isAnswered", true).await()
    }

    suspend fun addEncouragement(prayerId: String, authorId: String, authorName: String, text: String, audioUrl: String? = null) {
        val encouragement = PrayerEncouragement(
            authorId = authorId, authorName = authorName, text = text,
            audioUrl = audioUrl, timestamp = System.currentTimeMillis(),
        )
        collection.document(prayerId).update("encouragements", FieldValue.arrayUnion(encouragement)).await()
    }
}
