package com.gracelink.android.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ArticleDao
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.dao.ChurchMemberDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ArticleEntity
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.MemberStatus
import com.gracelink.android.data.repository.ChurchAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InsightsState(
    val churchName: String = "",
    val memberCount: Int = 0,
    val pendingMemberCount: Int = 0,
    val articleCount: Int = 0,
    val totalLikes: Int = 0,
    val totalComments: Int = 0,
    val podcastEpisodeCount: Int = 0,
    val eventCount: Int = 0,
    val upcomingEventCount: Int = 0,
    val groupCount: Int = 0,
    val groupMemberTotal: Int = 0,
    val announcementCount: Int = 0,
)

private data class BaseFour(
    val churchName: String,
    val members: List<ChurchMemberEntity>,
    val articles: List<ArticleEntity>,
    val episodeCount: Int,
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val churchDao: ChurchDao,
    private val userDao: UserDao,
    private val memberDao: ChurchMemberDao,
    private val articleDao: ArticleDao,
    private val podcastDao: PodcastDao,
    private val eventDao: ChurchEventDao,
    private val adminRepo: ChurchAdminRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<InsightsState> = userDao.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) flowOf(InsightsState())
        else churchDao.byOwner(uid).flatMapLatest { church ->
            if (church == null) {
                flowOf(InsightsState())
            } else {
                val churchId = church.id

                val baseFlow = combine(
                    memberDao.forChurch(churchId),
                    articleDao.forAuthor(uid),
                    podcastDao.seriesByAuthor(uid).flatMapLatest { seriesList ->
                        if (seriesList.isEmpty()) flowOf(0)
                        else combine(seriesList.map { podcastDao.episodesFor(it.id) }) { arrays -> arrays.sumOf { it.size } }
                    },
                ) { members, articles, episodeCount ->
                    BaseFour(church.name, members, articles, episodeCount)
                }

                combine(baseFlow, eventDao.forChurch(churchId), adminRepo.groups(churchId), adminRepo.announcements(churchId)) { base, events, groups, announcements ->
                    val now = System.currentTimeMillis()
                    InsightsState(
                        churchName = base.churchName,
                        memberCount = base.members.count { it.status == MemberStatus.APPROVED },
                        pendingMemberCount = base.members.count { it.status == MemberStatus.PENDING },
                        articleCount = base.articles.size,
                        totalLikes = base.articles.sumOf { it.likeCount },
                        totalComments = base.articles.sumOf { it.commentCount },
                        podcastEpisodeCount = base.episodeCount,
                        eventCount = events.size,
                        upcomingEventCount = events.count { it.startTime > now },
                        groupCount = groups.size,
                        groupMemberTotal = groups.sumOf { it.memberCount },
                        announcementCount = announcements.size,
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsState())
}
