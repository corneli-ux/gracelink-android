package com.gracelink.android.data.repository

import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.BeliefSystem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class CloudProfileEntry(
    val accountType: AccountType,
    val displayName: String,
    val beliefSystem: BeliefSystem,
    val bio: String?,
    val photoUrl: String?,
    // Church-only fields (null for Personal/Pastor)
    val churchDescription: String?,
    val churchPastorName: String?,
    val churchLocation: String?,
    val churchWebsite: String?,
    val churchPhone: String?,
)

data class PastorProfile(
    val uid: String,
    val displayName: String,
    val beliefSystem: BeliefSystem,
    val bio: String?,
    val photoUrl: String?,
)

/**
 * A small Firestore-backed registry mapping a Firebase UID to enough of
 * their profile (and, for churches, their church record) to fully
 * reconstruct it after a reinstall or new device.
 *
 * This exists because local Room restoration (ProfileGateViewModel) only
 * works if the local database survives -- true across a simple sign-out,
 * but NOT across an app reinstall or a new device, since that wipes the
 * entire local SQLite file, including the very church/podcast records
 * restoration would otherwise read from. This is deliberately scoped to
 * "enough to route correctly and not look broken" (identity + the church
 * record's own fields), not a full offline sync of articles/podcasts/
 * members -- that's a bigger project of its own.
 *
 * Also doubles as the only cross-user directory in the app: Room's
 * UserDao only ever holds the CURRENT device's signed-in user, so finding
 * OTHER users (e.g. browsing individual pastors to follow) has to go
 * through this Firestore collection instead.
 */
@Singleton
class CloudProfileRegistry @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection = firestore.collection("users_registry")
    private fun docFor(uid: String) = collection.document(uid)

    suspend fun writePersonal(uid: String, displayName: String, beliefSystem: BeliefSystem) {
        writeSafely(
            uid, mapOf(
                "accountType" to AccountType.PERSONAL.name,
                "displayName" to displayName,
                "beliefSystem" to beliefSystem.name,
                "updatedAt" to System.currentTimeMillis(),
            )
        )
    }

    suspend fun writePastor(uid: String, displayName: String, beliefSystem: BeliefSystem, bio: String? = null) {
        writeSafely(
            uid, mapOf(
                "accountType" to AccountType.PASTOR.name,
                "displayName" to displayName,
                "beliefSystem" to beliefSystem.name,
                "bio" to (bio ?: ""),
                "updatedAt" to System.currentTimeMillis(),
            )
        )
    }

    suspend fun writeChurch(
        uid: String,
        churchName: String,
        pastorName: String,
        location: String,
        beliefSystem: BeliefSystem,
        description: String,
        website: String?,
        phone: String?,
    ) {
        writeSafely(
            uid, mapOf(
                "accountType" to AccountType.CHURCH.name,
                "displayName" to churchName,
                "beliefSystem" to beliefSystem.name,
                "bio" to description,
                "churchDescription" to description,
                "churchPastorName" to pastorName,
                "churchLocation" to location,
                "churchWebsite" to (website ?: ""),
                "churchPhone" to (phone ?: ""),
                "updatedAt" to System.currentTimeMillis(),
            )
        )
    }

    private suspend fun writeSafely(uid: String, data: Map<String, Any>) {
        try {
            docFor(uid).set(data).await()
        } catch (_: Exception) {
            // Best-effort -- if this fails (offline, etc.), local restoration
            // still works for the plain sign-out case, so don't block
            // registration on a cloud write succeeding.
        }
    }

    /** Partial update -- doesn't disturb name/bio/church fields already
     * written by writePersonal/writePastor/writeChurch. Requires the
     * profile document to already exist (it does, by the time someone
     * uploads a photo, since registration writes it first). */
    suspend fun updatePhotoUrl(uid: String, photoUrl: String) {
        try {
            docFor(uid).update("photoUrl", photoUrl).await()
        } catch (_: Exception) {
            // Best-effort, same reasoning as writeSafely -- a failed cloud
            // sync of the photo URL shouldn't block the local upload from
            // completing and showing on the user's own device.
        }
    }

    suspend fun read(uid: String): CloudProfileEntry? {
        return try {
            val s = docFor(uid).get().await()
            val typeStr = s.getString("accountType") ?: return null
            val name = s.getString("displayName") ?: return null
            CloudProfileEntry(
                accountType = AccountType.valueOf(typeStr),
                displayName = name,
                beliefSystem = s.getString("beliefSystem")?.let { runCatching { BeliefSystem.valueOf(it) }.getOrNull() } ?: BeliefSystem.NONDENOMINATIONAL,
                bio = s.getString("bio"),
                photoUrl = s.getString("photoUrl"),
                churchDescription = s.getString("churchDescription"),
                churchPastorName = s.getString("churchPastorName"),
                churchLocation = s.getString("churchLocation"),
                churchWebsite = s.getString("churchWebsite"),
                churchPhone = s.getString("churchPhone"),
            )
        } catch (_: Exception) {
            null
        }
    }

    /** Every individual pastor account, for the pastor discovery screen --
     * the only place in the app that needs to browse users across devices. */
    fun allPastors(): Flow<List<PastorProfile>> = callbackFlow {
        val registration = collection
            .whereEqualTo("accountType", AccountType.PASTOR.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val pastors = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("displayName") ?: return@mapNotNull null
                    val belief = doc.getString("beliefSystem")?.let { runCatching { BeliefSystem.valueOf(it) }.getOrNull() } ?: BeliefSystem.NONDENOMINATIONAL
                    PastorProfile(uid = doc.id, displayName = name, beliefSystem = belief, bio = doc.getString("bio")?.ifBlank { null }, photoUrl = doc.getString("photoUrl"))
                } ?: emptyList()
                trySend(pastors)
            }
        awaitClose { registration.remove() }
    }

    suspend fun getPastor(uid: String): PastorProfile? {
        val entry = read(uid) ?: return null
        if (entry.accountType != AccountType.PASTOR) return null
        return PastorProfile(uid = uid, displayName = entry.displayName, beliefSystem = entry.beliefSystem, bio = entry.bio?.ifBlank { null }, photoUrl = entry.photoUrl)
    }
}
