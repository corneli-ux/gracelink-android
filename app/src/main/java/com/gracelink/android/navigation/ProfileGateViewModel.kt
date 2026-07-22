package com.gracelink.android.navigation

import androidx.lifecycle.ViewModel
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.data.db.entity.ChurchEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.UserEntity
import com.gracelink.android.data.db.entity.VerificationStatus
import com.gracelink.android.data.repository.CloudProfileRegistry
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Answers one question for the nav graph at the Splash/Onboarding boundary:
 * has a profile been set up yet, and if so, what account type is it? Used
 * to make "Set Up Profile" a mandatory step before the app opens, and to
 * route straight into the right portal (Church/Pastor/member Home)
 * afterward instead of always landing on the generic Home.
 *
 * Restoration happens in three tiers, each covering a different way the
 * local "current profile" row can go missing while the account itself
 * still legitimately exists:
 *   1. Local profile matching the signed-in Firebase UID -- the normal case.
 *   2. Local Room data still has the church/podcast records (survives a
 *      plain sign-out, which only clears the profile row) -- reconstruct
 *      the profile from those.
 *   3. Local Room data is GONE entirely (app reinstall / new device wipes
 *      the whole SQLite file) -- fall back to the Firestore cloud registry
 *      and reconstruct both the profile AND the church record from there.
 */
@HiltViewModel
class ProfileGateViewModel @Inject constructor(
    private val userDao: UserDao,
    private val churchDao: ChurchDao,
    private val podcastDao: PodcastDao,
    private val cloudRegistry: CloudProfileRegistry,
) : ViewModel() {

    /** Null if no profile has been set up yet. */
    suspend fun currentAccountType(): AccountType? = userDao.currentOnce()?.accountType

    /**
     * FirebaseAuth.getInstance().currentUser can legitimately return null
     * immediately after app start, before the auth object has finished
     * initializing and restoring the persisted session from disk --
     * this is documented directly in Firebase's own currentUser docs.
     * restoreOrCheckProfile() used to read currentUser synchronously the
     * moment the splash screen completed, which is exactly the small
     * window where that race is most likely to lose -- reading null
     * before Firebase had actually restored the real, valid session,
     * which would incorrectly route a genuinely signed-in user to the
     * Auth screen ("logged out on every close"). Waiting for the first
     * AuthStateListener callback guarantees Firebase has actually
     * settled on the real auth state before this check runs at all.
     */
    private suspend fun currentFirebaseUser(): com.google.firebase.auth.FirebaseUser? {
        val auth = FirebaseAuth.getInstance()
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val listener = object : FirebaseAuth.AuthStateListener {
                override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                    firebaseAuth.removeAuthStateListener(this)
                    if (cont.isActive) cont.resume(firebaseAuth.currentUser)
                }
            }
            auth.addAuthStateListener(listener)
            cont.invokeOnCancellation { auth.removeAuthStateListener(listener) }
        }
    }

    suspend fun restoreOrCheckProfile(): AccountType? {
        val fbUser = currentFirebaseUser()
        val existing = userDao.currentOnce()

        // Tier 1: local profile belongs to the currently signed-in account.
        if (existing != null && (fbUser == null || existing.uid == fbUser.uid)) {
            return existing.accountType
        }

        if (fbUser == null) return null
        val uid = fbUser.uid

        // Tier 2: local Room still has the church/podcast records (a plain
        // sign-out only clears the profile row, not these).
        val church = churchDao.byOwnerOnce(uid)
        if (church != null) {
            restoreUser(uid, church.name, fbUser.email ?: "", fbUser.photoUrl?.toString(), AccountType.CHURCH, church.beliefSystem, church.description)
            return AccountType.CHURCH
        }

        val pastorSeries = podcastDao.seriesByAuthor(uid).first().firstOrNull { it.authorType == AccountType.PASTOR }
        if (pastorSeries != null) {
            restoreUser(uid, pastorSeries.authorName, fbUser.email ?: "", fbUser.photoUrl?.toString(), AccountType.PASTOR, BeliefSystem.NONDENOMINATIONAL, null)
            return AccountType.PASTOR
        }

        // Tier 3: local Room data is gone entirely (reinstall / new device).
        // Fall back to the cloud registry and reconstruct everything needed.
        val cloud = cloudRegistry.read(uid)
        if (cloud != null) {
            restoreUser(uid, cloud.displayName, fbUser.email ?: "", fbUser.photoUrl?.toString(), cloud.accountType, cloud.beliefSystem, cloud.bio)
            if (cloud.accountType == AccountType.CHURCH) {
                restoreChurchFromCloud(uid, cloud)
            }
            return cloud.accountType
        }

        return null
    }

    private suspend fun restoreUser(
        uid: String,
        displayName: String,
        email: String,
        photoUrl: String?,
        accountType: AccountType,
        beliefSystem: BeliefSystem,
        bio: String?,
    ) {
        userDao.upsert(
            UserEntity(
                uid = uid,
                displayName = displayName,
                email = email,
                photoUrl = photoUrl,
                preferredLanguage = ContentLanguage.EN,
                createdAt = System.currentTimeMillis(),
                totalMinutes = 0,
                completedItems = 0,
                prayersOffered = 0,
                streakDays = 0,
                dataSaverEnabled = false,
                notificationsEnabled = true,
                accountType = accountType,
                beliefSystem = beliefSystem,
                churchId = null,
                isVerified = false,
                bio = bio,
            )
        )
    }

    private suspend fun restoreChurchFromCloud(uid: String, cloud: com.gracelink.android.data.repository.CloudProfileEntry) {
        // Local church row is gone too (that's why we got here) -- recreate
        // it from the cloud backup so Church Portal has real data again
        // instead of showing "Set up your profile" despite being CHURCH type.
        val existing = churchDao.byOwnerOnce(uid)
        if (existing != null) return
        val now = System.currentTimeMillis()
        churchDao.insert(
            ChurchEntity(
                id = "church_$uid",
                name = cloud.displayName,
                description = cloud.churchDescription ?: cloud.bio ?: "",
                pastorName = cloud.churchPastorName ?: "",
                location = cloud.churchLocation ?: "",
                beliefSystem = cloud.beliefSystem,
                verificationStatus = VerificationStatus.PENDING,
                certificateUrl = null,
                photoUrl = null,
                memberCount = 0,
                createdAt = now,
                gracePeriodEndsAt = now + 30L * 24 * 3600 * 1000,
                website = cloud.churchWebsite?.ifBlank { null },
                phone = cloud.churchPhone?.ifBlank { null },
                ownerUserId = uid,
            )
        )
    }
}
