package com.gracelink.android.data.repository

import com.gracelink.android.feature.audioconnect.AudioSpace
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

/**
 * Real, multi-user live audio spaces backed by Firestore. Previously this
 * was a 100% in-memory fake list seeded with fictional hosts and crowd
 * sizes on every ViewModel creation -- a space one person "created" was
 * never visible to anyone else, and nothing survived an app restart.
 * Firestore's real-time listener means every device sees the same live
 * list update instantly.
 *
 * Honest scope note: this makes the space LISTING, creation, and
 * participant COUNT real and shared. It does not implement actual voice
 * audio transmission between participants (that needs a WebRTC/signaling
 * infrastructure investment of its own) -- the mic/hand-raise controls
 * remain local UI state, same as before.
 */
@Singleton
class LiveSpaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("live_spaces")

    fun activeSpaces(): Flow<List<AudioSpace>> = callbackFlow {
        val registration = collection
            .whereEqualTo("isLive", true)
            .orderBy("startedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val spaces = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<AudioSpace>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(spaces)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createSpace(title: String, topic: String, hostId: String, hostName: String): String {
        val id = "space_${System.currentTimeMillis()}"
        val space = AudioSpace(
            id = id, title = title, hostId = hostId, hostName = hostName,
            topic = topic, participantCount = 1, isLive = true,
            startedAt = System.currentTimeMillis(),
        )
        collection.document(id).set(space).await()
        return id
    }

    suspend fun joinSpace(id: String) {
        collection.document(id).update("participantCount", FieldValue.increment(1)).await()
    }

    suspend fun leaveSpace(id: String) {
        collection.document(id).update("participantCount", FieldValue.increment(-1)).await()
    }

    suspend fun endSpace(id: String) {
        collection.document(id).update("isLive", false).await()
    }
}
