package com.gracelink.android.feature.churches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.ChurchMemberDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ChurchEntity
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.VerificationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChurchesState(
    val churches: List<ChurchEntity> = emptyList(),
    val myChurchId: String? = null,
)

@HiltViewModel
class ChurchesViewModel @Inject constructor(
    private val churchDao: ChurchDao,
    private val memberDao: ChurchMemberDao,
    private val userDao: UserDao,
) : ViewModel() {

    val state: StateFlow<ChurchesState> = combine(
        churchDao.all(), userDao.current()
    ) { churches, user ->
        ChurchesState(churches, user?.churchId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChurchesState())

    fun joinChurch(church: ChurchEntity) = viewModelScope.launch {
        val user = userDao.currentOnce() ?: return@launch
        val memberId = "${church.id}_${user.uid}"
        memberDao.insert(ChurchMemberEntity(
            id = memberId, churchId = church.id, userId = user.uid,
            displayName = user.displayName, joinedAt = System.currentTimeMillis(),
            beliefSystem = user.beliefSystem, isActive = true,
        ))
    }

    fun createChurch(name: String, pastor: String, location: String, belief: com.gracelink.android.data.db.entity.BeliefSystem, email: String) = viewModelScope.launch {
        val churchId = "church_${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        churchDao.insert(ChurchEntity(
            id = churchId, name = name, description = "Church pastored by $pastor in $location",
            pastorName = pastor, location = location, beliefSystem = belief,
            verificationStatus = VerificationStatus.PENDING,
            certificateUrl = null, photoUrl = null, memberCount = 1,
            createdAt = now, gracePeriodEndsAt = now + 30L * 24 * 3600 * 1000,
            website = null, phone = null,
        ))
        val user = userDao.currentOnce()
        if (user != null) {
            joinChurch(ChurchEntity(churchId, name, "", pastor, location, belief, VerificationStatus.PENDING, null, null, 1, now, now + 30L * 24 * 3600 * 1000, null, null))
        }
    }
}
