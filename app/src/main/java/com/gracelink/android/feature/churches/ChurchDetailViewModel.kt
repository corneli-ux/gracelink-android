package com.gracelink.android.feature.churches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.*
import com.gracelink.android.data.db.entity.*
import com.gracelink.android.data.repository.ChurchAdminRepository
import com.gracelink.android.data.repository.CollaborationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChurchDetailState(
    val church: ChurchEntity? = null,
    val members: List<ChurchMemberEntity> = emptyList(),
    val events: List<ChurchEventEntity> = emptyList(),
    val articles: List<ArticleEntity> = emptyList(),
    val announcements: List<AnnouncementEntity> = emptyList(),
    val podcasts: List<PodcastSeriesEntity> = emptyList(),
    val myMembership: ChurchMemberEntity? = null,
    val myId: String = "u_demo",
    val myName: String = "",
    val myAccountType: AccountType = AccountType.PERSONAL,
    val myCollaborationRequest: CollaborationRequestEntity? = null,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
private data class BaseDetail(
    val church: ChurchEntity?,
    val members: List<ChurchMemberEntity>,
    val events: List<ChurchEventEntity>,
    val articles: List<ArticleEntity>,
    val uid: String,
)

@HiltViewModel
class ChurchDetailViewModel @Inject constructor(
    private val churchDao: ChurchDao,
    private val memberDao: ChurchMemberDao,
    private val eventDao: ChurchEventDao,
    private val articleDao: ArticleDao,
    private val podcastDao: PodcastDao,
    private val userDao: UserDao,
    private val collaborationRepo: CollaborationRepository,
    private val adminRepo: ChurchAdminRepository,
) : ViewModel() {

    private val churchId = MutableStateFlow("")
    private val myId = MutableStateFlow("u_demo")
    private val myName = MutableStateFlow("")
    private val myAccountType = MutableStateFlow(AccountType.PERSONAL)

    init {
        viewModelScope.launch {
            userDao.current().collect { u ->
                myId.value = u?.uid ?: "u_demo"
                myName.value = u?.displayName ?: ""
                myAccountType.value = u?.accountType ?: AccountType.PERSONAL
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val baseFlow = churchId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(BaseDetail(null, emptyList(), emptyList(), emptyList(), "u_demo"))
        else combine(
            flowOf(churchDao.getById(id)),
            memberDao.forChurch(id),
            eventDao.forChurch(id),
            articleDao.forChurch(id),
            myId,
        ) { church, members, events, articles, uid ->
            BaseDetail(church, members, events, articles, uid)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val contentFlow = baseFlow.flatMapLatest { base ->
        val ownerUid = base.church?.ownerUserId
        combine(
            if (ownerUid == null) flowOf(emptyList()) else adminRepo.announcements(base.church.id),
            if (ownerUid == null) flowOf(emptyList()) else podcastDao.seriesByAuthor(ownerUid),
        ) { announcements, podcasts -> announcements to podcasts }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val state: StateFlow<ChurchDetailState> = combine(baseFlow, contentFlow) { base, content ->
        base to content
    }.flatMapLatest { (base, content) ->
        collaborationRepo.sentBy(base.uid).map { sent ->
            ChurchDetailState(
                church = base.church,
                members = base.members,
                events = base.events,
                articles = base.articles,
                announcements = content.first,
                podcasts = content.second,
                myMembership = base.members.firstOrNull { it.userId == base.uid },
                myId = base.uid,
                myName = myName.value,
                myAccountType = myAccountType.value,
                myCollaborationRequest = sent.firstOrNull { it.toChurchId == base.church?.id },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChurchDetailState())

    fun load(id: String) { churchId.value = id }

    fun joinChurch() = viewModelScope.launch {
        val church = state.value.church ?: return@launch
        val user = userDao.currentOnce() ?: return@launch
        memberDao.insert(ChurchMemberEntity(
            id = "${church.id}_${user.uid}",
            churchId = church.id,
            userId = user.uid,
            displayName = user.displayName,
            joinedAt = System.currentTimeMillis(),
            beliefSystem = user.beliefSystem,
            isActive = false,  // inactive until approved
            status = MemberStatus.PENDING,
            approvedAt = null,
        ))
    }

    /** Churches/pastors don't join each other as members -- they propose partnering on events, debates, or discussions. */
    fun requestCollaboration(message: String, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val church = state.value.church ?: return@launch
        val user = userDao.currentOnce() ?: return@launch
        val ok = collaborationRepo.requestCollaboration(
            fromUid = user.uid,
            fromName = user.displayName,
            fromType = user.accountType,
            toChurchId = church.id,
            toChurchName = church.name,
            message = message,
        )
        onDone(ok)
    }

    fun approveMember(memberId: String) = viewModelScope.launch {
        memberDao.approve(memberId, System.currentTimeMillis())
    }

    fun rejectMember(memberId: String) = viewModelScope.launch {
        memberDao.reject(memberId)
    }
}
