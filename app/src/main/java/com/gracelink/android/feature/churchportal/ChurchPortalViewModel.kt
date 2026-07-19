package com.gracelink.android.feature.churchportal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ArticleDao
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.ChurchMemberDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ChurchEntity
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
)

@HiltViewModel
class ChurchPortalViewModel @Inject constructor(
    private val userDao: UserDao,
    private val churchDao: ChurchDao,
    private val memberDao: ChurchMemberDao,
    private val articleDao: ArticleDao,
    private val podcastDao: PodcastDao,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ChurchPortalState> = userDao.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) {
            flowOf(ChurchPortalState())
        } else {
            churchDao.byOwner(uid).flatMapLatest { church ->
                if (church == null) {
                    flowOf(ChurchPortalState(myUid = uid))
                } else {
                    combine(
                        memberDao.pendingForChurch(church.id),
                        memberDao.approvedForChurch(church.id),
                        articleDao.forAuthor(uid),
                        podcastDao.seriesByAuthor(uid),
                    ) { pending, approved, articles, podcasts ->
                        ChurchPortalState(
                            myUid = uid,
                            church = church,
                            pendingMembers = pending,
                            approvedCount = approved.size,
                            articleCount = articles.size,
                            podcastCount = podcasts.size,
                        )
                    }
                }
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
}
