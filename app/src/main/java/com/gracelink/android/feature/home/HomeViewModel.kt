package com.gracelink.android.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.ChurchMemberDao
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.db.entity.LiveSessionStatus
import com.gracelink.android.data.db.entity.MemberStatus
import com.gracelink.android.data.repository.ChurchActivityItem
import com.gracelink.android.data.repository.ChurchActivityRepository
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.LiveSessionRepository
import com.gracelink.android.data.repository.LiveSpaceRepository
import com.gracelink.android.data.repository.UserRepository
import com.gracelink.android.feature.audioconnect.AudioSpace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeState(
    val greeting: String = "",
    val userName: String = "",
    val liveRadio: List<ContentEntity> = emptyList(),
    val liveSession: LiveSessionEntity? = null,
    val liveSpace: AudioSpace? = null,
    val continueListening: List<ContentEntity> = emptyList(),
    // Raw library, not pre-shuffled -- shuffling belongs at the UI layer
    // (gated by remember(library)) so it only re-runs when the library
    // itself actually changes, not on every unrelated state emission
    // (e.g. a live space starting) the way a shuffle computed here would.
    val library: List<ContentEntity> = emptyList(),
    val churchName: String? = null,
    val churchActivity: List<ChurchActivityItem> = emptyList(),
)

private data class HomeBase(
    val liveRadio: List<ContentEntity>,
    val library: List<ContentEntity>,
    val liveSession: LiveSessionEntity?,
    val liveSpace: AudioSpace?,
    val userName: String,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepo: ContentRepository,
    private val liveRepo: LiveSessionRepository,
    private val liveSpaceRepo: LiveSpaceRepository,
    private val memberDao: ChurchMemberDao,
    private val churchDao: ChurchDao,
    private val activityRepo: ChurchActivityRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val baseFlow = combine(
        contentRepo.liveRadio(),
        contentRepo.library(),
        liveRepo.byStatus(LiveSessionStatus.LIVE),
        liveSpaceRepo.activeSpaces(),
        userRepo.current(),
    ) { liveRadio, library, liveSessions, liveSpaces, user ->
        HomeBase(liveRadio, library, liveSessions.firstOrNull(), liveSpaces.firstOrNull(), user?.displayName ?: "")
    }

    /** A member's home should surface what their own church has posted --
     * this is what makes the app feel alive/worth checking daily, rather
     * than just a static content library. Only shown once membership is
     * actually approved, not just requested. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val churchActivityFlow = userRepo.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) flowOf(Triple<String?, String?, List<ChurchActivityItem>>(null, null, emptyList()))
        else memberDao.forUser(uid).flatMapLatest { memberships ->
            val approved = memberships.firstOrNull { it.status == MemberStatus.APPROVED }
            if (approved == null) flowOf(Triple<String?, String?, List<ChurchActivityItem>>(null, null, emptyList()))
            else {
                val churchName = churchDao.getById(approved.churchId)?.name
                activityRepo.feedFor(approved.churchId).map { items -> Triple(approved.churchId, churchName, items) }
            }
        }
    }

    val state: StateFlow<HomeState> = combine(baseFlow, churchActivityFlow) { base, churchInfo ->
        HomeState(
            greeting = greetingFor(),
            userName = base.userName,
            liveRadio = base.liveRadio,
            liveSession = base.liveSession,
            liveSpace = base.liveSpace,
            continueListening = base.library.take(3),
            library = base.library,
            churchName = churchInfo.second,
            churchActivity = churchInfo.third.take(8),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    private fun greetingFor(): String {
        val hour = java.time.LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Peace be with you"
        }
    }
}
