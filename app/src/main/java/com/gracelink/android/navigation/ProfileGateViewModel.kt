package com.gracelink.android.navigation

import androidx.lifecycle.ViewModel
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Answers one question for the nav graph at the Splash/Onboarding boundary:
 * has a profile been set up yet, and if so, what account type is it? Used
 * to make "Set Up Profile" a mandatory step before the app opens, and to
 * route straight into the right portal (Church/Pastor/member Home)
 * afterward instead of always landing on the generic Home.
 *
 * Also fixes a real data-loss bug: signing out only ever deleted the local
 * "current profile" row -- it never touched the church/podcast/article
 * records tied to that Firebase account. But since routing decisions only
 * checked that local row, signing back in looked identical to being a
 * brand-new user: forced back through full Registration, with the
 * person's actual church/portal now orphaned (still in the database,
 * just unreachable because nothing pointed back to it).
 */
@HiltViewModel
class ProfileGateViewModel @Inject constructor(
    private val userDao: UserDao,
    private val churchDao: ChurchDao,
    private val podcastDao: PodcastDao,
) : ViewModel() {

    /** Null if no profile has been set up yet. */
    suspend fun currentAccountType(): AccountType? = userDao.currentOnce()?.accountType

    /**
     * Called right after a successful sign-in. If a local profile already
     * exists, returns its type immediately. If not, checks whether this
     * Firebase account already owns a church or has published podcasts as
     * a pastor, and reconstructs the local profile from that real data
     * instead of forcing Registration again. Returns null only if this
     * really is a brand-new account with no prior church/pastor footprint.
     */
    suspend fun restoreOrCheckProfile(): AccountType? {
        val existing = userDao.currentOnce()
        if (existing != null) return existing.accountType

        val fbUser = FirebaseAuth.getInstance().currentUser ?: return null
        val uid = fbUser.uid

        val church = churchDao.byOwnerOnce(uid)
        if (church != null) {
            restoreUser(
                uid = uid,
                displayName = church.name,
                email = fbUser.email ?: "",
                photoUrl = fbUser.photoUrl?.toString(),
                accountType = AccountType.CHURCH,
                beliefSystem = church.beliefSystem,
                bio = church.description,
            )
            return AccountType.CHURCH
        }

        val pastorSeries = podcastDao.seriesByAuthor(uid).first().firstOrNull { it.authorType == AccountType.PASTOR }
        if (pastorSeries != null) {
            restoreUser(
                uid = uid,
                displayName = pastorSeries.authorName,
                email = fbUser.email ?: "",
                photoUrl = fbUser.photoUrl?.toString(),
                accountType = AccountType.PASTOR,
                beliefSystem = com.gracelink.android.data.db.entity.BeliefSystem.NONDENOMINATIONAL,
                bio = null,
            )
            return AccountType.PASTOR
        }

        return null
    }

    private suspend fun restoreUser(
        uid: String,
        displayName: String,
        email: String,
        photoUrl: String?,
        accountType: AccountType,
        beliefSystem: com.gracelink.android.data.db.entity.BeliefSystem,
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
}
