package com.gracelink.android.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ArticleDao
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PastorInsightsState(
    val displayName: String = "",
    val articleCount: Int = 0,
    val totalLikes: Int = 0,
    val totalComments: Int = 0,
    val podcastEpisodeCount: Int = 0,
    val eventCount: Int = 0,
    val upcomingEventCount: Int = 0,
)

/**
 * Same idea as InsightsViewModel (real counts, no fabricated numbers) but
 * for Individual Pastor accounts, which don't have a ChurchEntity to
 * resolve through -- pastor-hosted content and events are keyed by the
 * pastor's own uid directly (the same convention EventCreateViewModel
 * and radio/podcast hosting already use), not a church id.
 */
@HiltViewModel
class PastorInsightsViewModel @Inject constructor(
    private val userDao: UserDao,
    private val articleDao: ArticleDao,
    private val podcastDao: PodcastDao,
    private val eventDao: ChurchEventDao,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<PastorInsightsState> = userDao.current().flatMapLatest { user ->
        if (user == null) {
            flowOf(PastorInsightsState())
        } else {
            val uid = user.uid
            combine(
                articleDao.forAuthor(uid),
                podcastDao.seriesByAuthor(uid).flatMapLatest { seriesList ->
                    if (seriesList.isEmpty()) flowOf(0)
                    else combine(seriesList.map { podcastDao.episodesFor(it.id) }) { arrays -> arrays.sumOf { it.size } }
                },
                eventDao.forChurch(uid),
            ) { articles, episodeCount, events ->
                val now = System.currentTimeMillis()
                PastorInsightsState(
                    displayName = user.displayName,
                    articleCount = articles.size,
                    totalLikes = articles.sumOf { it.likeCount },
                    totalComments = articles.sumOf { it.commentCount },
                    podcastEpisodeCount = episodeCount,
                    eventCount = events.size,
                    upcomingEventCount = events.count { it.startTime > now },
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PastorInsightsState())
}
