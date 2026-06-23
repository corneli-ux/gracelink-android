package com.gracelink.android.feature.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val userDao: UserDao,
) : ViewModel() {

    fun registerPersonal(name: String, email: String, belief: BeliefSystem, onComplete: () -> Unit) = viewModelScope.launch {
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
        onComplete()
    }

    fun registerChurch(name: String, pastorName: String, location: String, belief: BeliefSystem, email: String, onComplete: () -> Unit) = viewModelScope.launch {
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
        onComplete()
    }
}
