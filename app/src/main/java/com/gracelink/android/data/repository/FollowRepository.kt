package com.gracelink.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class FollowRecord(
    val id: String = "",
    val followerId: String = "",
    val followedId: String = "",   // a church id OR a pastor's own uid
    val followedName: String = "",
    val followedAt: Long = 0,
)

/**
 * Following is deliberately lighter-weight than ChurchMemberEntity
 * (membership): no approval needed, just a one-tap follow, and it works
 * for both churches and individual pastors from the same relationship --
 * this is what powers the Timeline feed, not congregational membership.
 */
@Singleton
class FollowRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("follows")

    private fun docId(followerId: String, followedId: String) = "${followerId}_$followedId"

    fun isFollowing(followerId: String, followedId: String): Flow<Boolean> = callbackFlow {
        val registration = collection.document(docId(followerId, followedId))
            .addSnapshotListener { snapshot, error ->
                trySend(error == null && snapshot?.exists() == true)
            }
        awaitClose { registration.remove() }
    }

    /** Every church/pastor id this person follows -- what the Timeline feed is built from. */
    fun followedIdsFor(followerId: String): Flow<List<String>> = callbackFlow {
        val registration = collection.whereEqualTo("followerId", followerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject<FollowRecord>()?.followedId } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    /** How many people follow this church/pastor -- shown on their own portal. */
    fun followerCount(followedId: String): Flow<Int> = callbackFlow {
        val registration = collection.whereEqualTo("followedId", followedId)
            .addSnapshotListener { snapshot, error ->
                trySend(if (error != null) 0 else (snapshot?.size() ?: 0))
            }
        awaitClose { registration.remove() }
    }

    suspend fun follow(followerId: String, followedId: String, followedName: String) {
        val record = FollowRecord(
            id = docId(followerId, followedId), followerId = followerId,
            followedId = followedId, followedName = followedName, followedAt = System.currentTimeMillis(),
        )
        collection.document(record.id).set(record).await()
    }

    suspend fun unfollow(followerId: String, followedId: String) {
        collection.document(docId(followerId, followedId)).delete().await()
    }
}
