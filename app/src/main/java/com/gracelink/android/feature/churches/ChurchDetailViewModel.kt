package com.gracelink.android.feature.churches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.*
import com.gracelink.android.data.db.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChurchDetailState(
    val church: ChurchEntity? = null,
    val members: List<ChurchMemberEntity> = emptyList(),
    val events: List<ChurchEventEntity> = emptyList(),
    val articles: List<ArticleEntity> = emptyList(),
    val myMembership: ChurchMemberEntity? = null,
    val myId: String = "u_demo",
)

@HiltViewModel
class ChurchDetailViewModel @Inject constructor(
    private val churchDao: ChurchDao,
    private val memberDao: ChurchMemberDao,
    private val eventDao: ChurchEventDao,
    private val articleDao: ArticleDao,
    private val userDao: UserDao,
) : ViewModel() {

    private val churchId = MutableStateFlow("")
    private val myId = MutableStateFlow("u_demo")

    init {
        viewModelScope.launch {
            userDao.current().collect { u -> myId.value = u?.uid ?: "u_demo" }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val state: StateFlow<ChurchDetailState> = churchId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(ChurchDetailState())
        else combine(
            flowOf(churchDao.getById(id)),
            memberDao.forChurch(id),
            eventDao.forChurch(id),
            articleDao.forChurch(id),
            myId
        ) { church, members, events, articles, uid ->
            ChurchDetailState(
                church = church,
                members = members,
                events = events,
                articles = articles,
                myMembership = members.firstOrNull { it.userId == uid },
                myId = uid,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChurchDetailState())

    fun load(id: String) { churchId.value = id }

    fun joinChurch() = viewModelScope.launch {
        val church = state.value.church ?: return@launch
        val uid = myId.value
        val user = userDao.currentOnce()
        memberDao.insert(ChurchMemberEntity(
            id = "${church.id}_$uid",
            churchId = church.id,
            userId = uid,
            displayName = user?.displayName ?: "Member",
            joinedAt = System.currentTimeMillis(),
            beliefSystem = user?.beliefSystem ?: BeliefSystem.NONDENOMINATIONAL,
            isActive = false,  // inactive until approved
            status = MemberStatus.PENDING,
            approvedAt = null,
        ))
    }

    fun approveMember(memberId: String) = viewModelScope.launch {
        memberDao.approve(memberId, System.currentTimeMillis())
    }

    fun rejectMember(memberId: String) = viewModelScope.launch {
        memberDao.reject(memberId)
    }

    fun createEvent(title: String, description: String, startTime: Long, isOnline: Boolean, meetingLink: String?, location: String?) = viewModelScope.launch {
        val church = state.value.church ?: return@launch
        eventDao.insert(ChurchEventEntity(
            id = "event_${System.currentTimeMillis()}",
            churchId = church.id,
            churchName = church.name,
            title = title,
            description = description,
            startTime = startTime,
            endTime = startTime + 7200000,
            isOnline = isOnline,
            meetingLink = meetingLink,
            location = location,
            category = "WORSHIP",
            attendeeCount = 0,
        ))
    }

    fun writeArticle(title: String, content: String) = viewModelScope.launch {
        val church = state.value.church ?: return@launch
        val user = userDao.currentOnce()
        articleDao.insert(ArticleEntity(
            id = "art_${System.currentTimeMillis()}",
            authorId = church.id,
            authorName = church.name,
            authorType = AccountType.CHURCH,
            churchId = church.id,
            title = title,
            content = content,
            publishedAt = System.currentTimeMillis(),
            likeCount = 0,
            commentCount = 0,
            tags = "",
        ))
    }
}

data class ChurchProfileState(
    val user: UserEntity? = null,
    val church: ChurchEntity? = null,
    val members: List<ChurchMemberEntity> = emptyList(),
    val pendingMembers: List<ChurchMemberEntity> = emptyList(),
    val events: List<ChurchEventEntity> = emptyList(),
    val articles: List<ArticleEntity> = emptyList(),
    val showCreateEvent: Boolean = false,
    val showWriteArticle: Boolean = false,
    val showVerification: Boolean = false,
)

@HiltViewModel
class ChurchProfileViewModel @Inject constructor(
    private val churchDao: ChurchDao,
    private val memberDao: ChurchMemberDao,
    private val eventDao: ChurchEventDao,
    private val articleDao: ArticleDao,
    private val userDao: UserDao,
) : ViewModel() {

    private val _showCreateEvent = MutableStateFlow(false)
    private val _showWriteArticle = MutableStateFlow(false)
    private val _showVerification = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val state: StateFlow<ChurchProfileState> = userDao.current().flatMapLatest { user ->
        val churchId = user?.churchId ?: user?.uid
        if (churchId == null) flowOf(ChurchProfileState(user = user))
        else {
            val membersFlow = combine(
                memberDao.approvedForChurch(churchId),
                memberDao.pendingForChurch(churchId)
            ) { approved, pending -> approved to pending }

            combine(
                flowOf(churchDao.getById(churchId)),
                membersFlow,
                eventDao.forChurch(churchId),
                articleDao.forChurch(churchId),
                combine(_showCreateEvent, _showWriteArticle, _showVerification) { e, a, v -> Triple(e, a, v) }
            ) { church, memberPair, events, articles, flags ->
                ChurchProfileState(
                    user = user,
                    church = church,
                    members = memberPair.first,
                    pendingMembers = memberPair.second,
                    events = events,
                    articles = articles,
                    showCreateEvent = flags.first,
                    showWriteArticle = flags.second,
                    showVerification = flags.third,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChurchProfileState())

    fun showCreateEvent(show: Boolean) { _showCreateEvent.value = show }
    fun showWriteArticle(show: Boolean) { _showWriteArticle.value = show }
    fun showVerification(show: Boolean) { _showVerification.value = show }

    fun createEvent(title: String, description: String, startTime: Long, isOnline: Boolean, link: String?, loc: String?) = viewModelScope.launch {
        val church = state.value.church ?: return@launch
        eventDao.insert(ChurchEventEntity(
            id = "event_${System.currentTimeMillis()}",
            churchId = church.id,
            churchName = church.name,
            title = title,
            description = description,
            startTime = startTime,
            endTime = startTime + 7200000,
            isOnline = isOnline,
            meetingLink = link,
            location = loc,
            category = "WORSHIP",
            attendeeCount = 0,
        ))
        _showCreateEvent.value = false
    }

    fun writeArticle(title: String, content: String) = viewModelScope.launch {
        val church = state.value.church ?: return@launch
        articleDao.insert(ArticleEntity(
            id = "art_${System.currentTimeMillis()}",
            authorId = church.id,
            authorName = church.name,
            authorType = AccountType.CHURCH,
            churchId = church.id,
            title = title,
            content = content,
            publishedAt = System.currentTimeMillis(),
            likeCount = 0,
            commentCount = 0,
            tags = "",
        ))
        _showWriteArticle.value = false
    }

    fun submitVerification(certificateUrl: String, photoUrl: String) = viewModelScope.launch {
        val church = state.value.church ?: return@launch
        churchDao.update(church.copy(
            certificateUrl = certificateUrl,
            photoUrl = photoUrl,
            verificationStatus = VerificationStatus.PENDING,
        ))
        _showVerification.value = false
    }

    fun approveMember(memberId: String) = viewModelScope.launch {
        memberDao.approve(memberId, System.currentTimeMillis())
    }

    fun rejectMember(memberId: String) = viewModelScope.launch {
        memberDao.reject(memberId)
    }
}
