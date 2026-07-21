package com.gracelink.android.feature.pastors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ArticleDao
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ArticleEntity
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import com.gracelink.android.data.repository.CloudProfileRegistry
import com.gracelink.android.data.repository.FollowRepository
import com.gracelink.android.data.repository.PastorProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PastorProfileState(
    val pastor: PastorProfile? = null,
    val articles: List<ArticleEntity> = emptyList(),
    val podcasts: List<PodcastSeriesEntity> = emptyList(),
    val events: List<ChurchEventEntity> = emptyList(),
    val myUid: String = "",
    val isFollowing: Boolean = false,
    val followerCount: Int = 0,
)

@HiltViewModel
class PastorProfileViewModel @Inject constructor(
    private val registry: CloudProfileRegistry,
    private val articleDao: ArticleDao,
    private val podcastDao: PodcastDao,
    private val eventDao: ChurchEventDao,
    private val userDao: UserDao,
    private val followRepo: FollowRepository,
) : ViewModel() {

    private val pastorUid = MutableStateFlow("")
    private val pastorFlow = MutableStateFlow<PastorProfile?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<PastorProfileState> = combine(
        pastorFlow, pastorUid.flatMapLatest { if (it.isBlank()) flowOf(emptyList()) else articleDao.forAuthor(it) },
        pastorUid.flatMapLatest { if (it.isBlank()) flowOf(emptyList()) else podcastDao.seriesByAuthor(it) },
        pastorUid.flatMapLatest { if (it.isBlank()) flowOf(emptyList()) else eventDao.forChurch(it) },
        userDao.current(),
    ) { pastor, articles, podcasts, events, me ->
        PastorProfileState(
            pastor = pastor, articles = articles, podcasts = podcasts, events = events,
            myUid = me?.uid ?: "",
        )
    }.let { base ->
        base.flatMapLatest { s ->
            val uid = pastorUid.value
            if (s.myUid.isBlank() || uid.isBlank()) {
                flowOf(s)
            } else {
                combine(followRepo.isFollowing(s.myUid, uid), followRepo.followerCount(uid)) { following, count ->
                    s.copy(isFollowing = following, followerCount = count)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PastorProfileState())

    fun load(uid: String) {
        pastorUid.value = uid
        viewModelScope.launch { pastorFlow.value = registry.getPastor(uid) }
    }

    fun toggleFollow() = viewModelScope.launch {
        val s = state.value
        val pastor = s.pastor ?: return@launch
        if (s.myUid.isBlank()) return@launch
        if (s.isFollowing) followRepo.unfollow(s.myUid, pastor.uid)
        else followRepo.follow(s.myUid, pastor.uid, pastor.displayName)
    }
}
