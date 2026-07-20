package com.gracelink.android.feature.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val userDao: UserDao,
    private val churchDao: ChurchDao,
    private val cloudRegistry: CloudProfileRegistry,
) : ViewModel() {

    fun registerPersonal(name: String, email: String, belief: BeliefSystem, onComplete: (AccountType) -> Unit) = viewModelScope.launch {
        // Use Firebase UID if available, otherwise generate one
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid ?: "u_${System.currentTimeMillis()}"
        val userEmail = firebaseUser?.email ?: email

        // Delete any existing demo user first
        userDao.deleteAll()

        val user = UserEntity(
            uid = uid,
            displayName = name,
            email = userEmail,
            photoUrl = firebaseUser?.photoUrl?.toString(),
            preferredLanguage = ContentLanguage.EN,
            createdAt = System.currentTimeMillis(),
            totalMinutes = 0,
            completedItems = 0,
            prayersOffered = 0,
            streakDays = 0,
            dataSaverEnabled = false,
            notificationsEnabled = true,
            accountType = AccountType.PERSONAL,
            beliefSystem = belief,
            churchId = null,
            isVerified = false,
            bio = null,
        )
        userDao.upsert(user)
        cloudRegistry.writePersonal(uid, name, belief)
        onComplete(AccountType.PERSONAL)
    }

    /**
     * Registering as a Church now also creates the actual ChurchEntity
     * record linked via ownerUserId -- previously this only created the
     * profile, leaving Church Portal with nothing real to manage.
     */
    fun registerChurch(name: String, pastorName: String, location: String, belief: BeliefSystem, email: String, onComplete: (AccountType) -> Unit) = viewModelScope.launch {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid ?: "church_${System.currentTimeMillis()}"
        val userEmail = firebaseUser?.email ?: email

        userDao.deleteAll()

        val user = UserEntity(
            uid = uid,
            displayName = name,
            email = userEmail,
            photoUrl = firebaseUser?.photoUrl?.toString(),
            preferredLanguage = ContentLanguage.EN,
            createdAt = System.currentTimeMillis(),
            totalMinutes = 0,
            completedItems = 0,
            prayersOffered = 0,
            streakDays = 0,
            dataSaverEnabled = false,
            notificationsEnabled = true,
            accountType = AccountType.CHURCH,
            beliefSystem = belief,
            churchId = null,
            isVerified = false,
            bio = "Church pastored by $pastorName in $location",
        )
        userDao.upsert(user)

        val existing = churchDao.byOwnerOnce(uid)
        if (existing == null) {
            val now = System.currentTimeMillis()
            churchDao.insert(
                ChurchEntity(
                    id = "church_$uid",
                    name = name,
                    description = "Church pastored by $pastorName in $location",
                    pastorName = pastorName,
                    location = location,
                    beliefSystem = belief,
                    verificationStatus = VerificationStatus.PENDING,
                    certificateUrl = null,
                    photoUrl = null,
                    memberCount = 0,
                    createdAt = now,
                    gracePeriodEndsAt = now + 30L * 24 * 3600 * 1000,
                    website = null,
                    phone = null,
                    ownerUserId = uid,
                )
            )
        }
        cloudRegistry.writeChurch(
            uid = uid, churchName = name, pastorName = pastorName, location = location,
            beliefSystem = belief, description = "Church pastored by $pastorName in $location",
            website = null, phone = null,
        )
        onComplete(AccountType.CHURCH)
    }

    fun registerPastor(name: String, email: String, belief: BeliefSystem, onComplete: (AccountType) -> Unit) = viewModelScope.launch {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid ?: "pastor_${System.currentTimeMillis()}"
        val userEmail = firebaseUser?.email ?: email

        userDao.deleteAll()

        val user = UserEntity(
            uid = uid,
            displayName = name,
            email = userEmail,
            photoUrl = firebaseUser?.photoUrl?.toString(),
            preferredLanguage = ContentLanguage.EN,
            createdAt = System.currentTimeMillis(),
            totalMinutes = 0,
            completedItems = 0,
            prayersOffered = 0,
            streakDays = 0,
            dataSaverEnabled = false,
            notificationsEnabled = true,
            accountType = AccountType.PASTOR,
            beliefSystem = belief,
            churchId = null,
            isVerified = false,
            bio = null,
        )
        userDao.upsert(user)
        cloudRegistry.writePastor(uid, name, belief)
        onComplete(AccountType.PASTOR)
    }
}
