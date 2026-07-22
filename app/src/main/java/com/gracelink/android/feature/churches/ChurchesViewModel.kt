package com.gracelink.android.feature.churches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.ChurchMemberDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.data.db.entity.ChurchEntity
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.MemberStatus
import com.gracelink.android.data.db.entity.VerificationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChurchesState(
    val churches: List<ChurchEntity> = emptyList(),
    val myChurchId: String? = null,
    val isGuest: Boolean = true,
    val myAccountType: AccountType = AccountType.PERSONAL,
)

@HiltViewModel
class ChurchesViewModel @Inject constructor(
    private val churchDao: ChurchDao,
    private val memberDao: ChurchMemberDao,
    private val userDao: UserDao,
) : ViewModel() {

    /**
     * myChurchId was derived from user?.churchId -- a field confirmed
     * dead elsewhere in this codebase (never set anywhere, by design;
     * membership is tracked via ChurchMemberDao, not a field on the user
     * row). That made this always null: the "My Church" filter tab
     * always returned an empty list, and every church card's "is this
     * my church" indicator was always false, for every user, regardless
     * of actual approved membership. Resolved properly via the same
     * pattern HomeViewModel already uses correctly.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ChurchesState> = combine(churchDao.all(), userDao.current()) { churches, user -> churches to user }
        .flatMapLatest { (churches, user) ->
            val uid = user?.uid
            if (uid == null) {
                flowOf(ChurchesState(churches = churches, myChurchId = null, isGuest = true, myAccountType = AccountType.PERSONAL))
            } else {
                memberDao.forUser(uid).map { memberships ->
                    val approvedChurchId = memberships.firstOrNull { it.status == MemberStatus.APPROVED }?.churchId
                    ChurchesState(churches = churches, myChurchId = approvedChurchId, isGuest = false, myAccountType = user.accountType)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChurchesState())

    /**
     * Was setting isActive = true at join time while status defaulted to
     * PENDING -- an inconsistent state confirmed wrong by how the DAO's
     * own approve()/reject() queries treat isActive strictly as meaning
     * "approved and active" (they always set it together with status).
     * A user joining from this screen specifically would end up active
     * before any admin approval, unlike joining via ChurchDetailViewModel
     * (the other, correct implementation of the same action), which this
     * now matches.
     */
    fun joinChurch(church: ChurchEntity) = viewModelScope.launch {
        val user = userDao.currentOnce() ?: return@launch
        val memberId = "${church.id}_${user.uid}"
        memberDao.insert(ChurchMemberEntity(
            id = memberId, churchId = church.id, userId = user.uid,
            displayName = user.displayName, joinedAt = System.currentTimeMillis(),
            beliefSystem = user.beliefSystem, isActive = false,
            status = MemberStatus.PENDING, approvedAt = null,
        ))
    }

    /**
     * Inserts the creator's own membership directly rather than reusing
     * joinChurch() -- that function correctly requires pending approval
     * for someone joining an existing church, but the person creating
     * the church is its owner and should never need to self-approve.
     * Reusing joinChurch() here would have made the creator a PENDING
     * member of their own brand-new church.
     */
    fun createChurch(name: String, pastor: String, location: String, belief: BeliefSystem, email: String) = viewModelScope.launch {
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
            memberDao.insert(ChurchMemberEntity(
                id = "${churchId}_${user.uid}", churchId = churchId, userId = user.uid,
                displayName = user.displayName, joinedAt = now,
                beliefSystem = user.beliefSystem, isActive = true,
                status = MemberStatus.APPROVED, approvedAt = now,
            ))
        }
    }
}
