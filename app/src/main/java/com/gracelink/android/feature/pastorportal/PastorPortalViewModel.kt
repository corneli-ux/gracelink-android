package com.gracelink.android.feature.pastorportal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ArticleDao
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class PastorPortalState(
    val me: UserEntity? = null,
    val articleCount: Int = 0,
    val podcastCount: Int = 0,
    val upcomingEvents: List<ChurchEventEntity> = emptyList(),
)

@HiltViewModel
class PastorPortalViewModel @javax.inject.Inject constructor(
    private val userDao: UserDao,
    private val articleDao: ArticleDao,
    private val podcastDao: PodcastDao,
    private val eventDao: ChurchEventDao,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<PastorPortalState> = userDao.current().flatMapLatest { user ->
        if (user == null) {
            flowOf(PastorPortalState())
        } else {
            combine(
                articleDao.forAuthor(user.uid),
                podcastDao.seriesByAuthor(user.uid),
                eventDao.forChurch(user.uid), // pastors host events under their own profile id
            ) { articles, podcasts, events ->
                PastorPortalState(me = user, articleCount = articles.size, podcastCount = podcasts.size, upcomingEvents = events)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PastorPortalState())
}
