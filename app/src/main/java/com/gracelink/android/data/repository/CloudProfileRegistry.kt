package com.gracelink.android.data.repository

import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.BeliefSystem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class CloudProfileEntry(
    val accountType: AccountType,
    val displayName: String,
    val beliefSystem: BeliefSystem,
    val bio: String?,
    // Church-only fields (null for Personal/Pastor)
    val churchDescription: String?,
    val churchPastorName: String?,
    val churchLocation: String?,
    val churchWebsite: String?,
    val churchPhone: String?,
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
 */
@Singleton
class CloudProfileRegistry @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun docFor(uid: String) = firestore.collection("users_registry").document(uid)

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

    suspend fun writePastor(uid: String, displayName: String, beliefSystem: BeliefSystem) {
        writeSafely(
            uid, mapOf(
                "accountType" to AccountType.PASTOR.name,
                "displayName" to displayName,
                "beliefSystem" to beliefSystem.name,
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
}
