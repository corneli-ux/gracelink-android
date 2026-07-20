package com.gracelink.android.feature.churchportal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ArticleDao
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.dao.ChurchMemberDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ChurchEntity
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.CollaborationRequestEntity
import com.gracelink.android.data.repository.CollaborationRepository
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

data class ChurchPortalState(
    val myUid: String? = null,
    val church: ChurchEntity? = null,
    val pendingMembers: List<ChurchMemberEntity> = emptyList(),
    val approvedCount: Int = 0,
    val articleCount: Int = 0,
    val podcastCount: Int = 0,
    val upcomingEvents: List<ChurchEventEntity> = emptyList(),
    val pendingCollaborations: List<CollaborationRequestEntity> = emptyList(),
)

private data class ChurchPortalBase(
    val myUid: String?,
    val church: ChurchEntity?,
    val pendingMembers: List<ChurchMemberEntity>,
    val approvedCount: Int,
    val articleCount: Int,
    val podcastCount: Int,
    val upcomingEvents: List<ChurchEventEntity>,
)

@HiltViewModel
class ChurchPortalViewModel @Inject constructor(
    private val userDao: UserDao,
    private val churchDao: ChurchDao,
    private val memberDao: ChurchMemberDao,
    private val articleDao: ArticleDao,
    private val podcastDao: PodcastDao,
    private val eventDao: ChurchEventDao,
    private val collaborationRepo: CollaborationRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val baseFlow = userDao.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) {
            flowOf(ChurchPortalBase(null, null, emptyList(), 0, 0, 0, emptyList()))
        } else {
            churchDao.byOwner(uid).flatMapLatest { church ->
                if (church == null) {
                    flowOf(ChurchPortalBase(uid, null, emptyList(), 0, 0, 0, emptyList()))
                } else {
                    combine(
                        memberDao.pendingForChurch(church.id),
                        memberDao.approvedForChurch(church.id),
                        articleDao.forAuthor(uid),
                        podcastDao.seriesByAuthor(uid),
                        eventDao.forChurch(church.id),
                    ) { pending, approved, articles, podcasts, events ->
                        ChurchPortalBase(
                            myUid = uid, church = church, pendingMembers = pending,
                            approvedCount = approved.size, articleCount = articles.size,
                            podcastCount = podcasts.size, upcomingEvents = events,
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ChurchPortalState> = baseFlow.flatMapLatest { base ->
        val churchId = base.church?.id
        if (churchId == null) {
            flowOf(
                ChurchPortalState(
                    myUid = base.myUid, church = base.church, pendingMembers = base.pendingMembers,
                    approvedCount = base.approvedCount, articleCount = base.articleCount,
                    podcastCount = base.podcastCount, upcomingEvents = base.upcomingEvents,
                )
            )
        } else {
            collaborationRepo.pendingFor(churchId).map { pendingCollabs ->
                ChurchPortalState(
                    myUid = base.myUid, church = base.church, pendingMembers = base.pendingMembers,
                    approvedCount = base.approvedCount, articleCount = base.articleCount,
                    podcastCount = base.podcastCount, upcomingEvents = base.upcomingEvents,
                    pendingCollaborations = pendingCollabs,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChurchPortalState())

    fun approveMember(memberId: String) = viewModelScope.launch {
        memberDao.approve(memberId, System.currentTimeMillis())
        state.value.church?.let { churchDao.incrementMemberCount(it.id) }
    }

    fun rejectMember(memberId: String) = viewModelScope.launch {
        memberDao.reject(memberId)
    }

    fun respondToCollaboration(id: String, accept: Boolean) = viewModelScope.launch {
        collaborationRepo.respond(id, accept)
    }
}
