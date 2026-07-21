package com.gracelink.android.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.repository.BiblicalReaction
import com.gracelink.android.data.repository.CloudProfileRegistry
import com.gracelink.android.data.repository.FollowRepository
import com.gracelink.android.data.repository.ReactionRepository
import com.gracelink.android.data.repository.TimelineCommentRepository
import com.gracelink.android.data.repository.TimelineItem
import com.gracelink.android.data.repository.TimelineRepository
import com.gracelink.android.data.repository.UserRepository
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

data class TimelineState(
    val items: List<TimelineItem> = emptyList(),
    val myUid: String = "",
    val myName: String = "",
    val isGuest: Boolean = true,
    val isFollowingAnyone: Boolean = false,
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timelineRepo: TimelineRepository,
    private val followRepo: FollowRepository,
    private val reactionRepo: ReactionRepository,
    private val commentRepo: TimelineCommentRepository,
    private val churchDao: ChurchDao,
    private val registry: CloudProfileRegistry,
    private val userRepo: UserRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TimelineState> = userRepo.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) {
            flowOf(TimelineState())
        } else {
            followRepo.followedIdsFor(uid).flatMapLatest { followedIds ->
                // Content is authored under the account's own uid (Article/Podcast/
                // Prayer/Question) OR under the church's own id (Event) -- these are
                // NOT the same value for a church. Following a church stores its
                // church.id, so without this enrichment, everything except Events
                // would never match. For each followed church id, resolve its real
                // owner uid too and match against both.
                val matchIdsFlow = kotlinx.coroutines.flow.flow {
                    val enriched = mutableSetOf<String>()
                    enriched.addAll(followedIds)
                    for (id in followedIds) {
                        churchDao.getById(id)?.ownerUserId?.let { enriched.add(it) }
                    }
                    emit(enriched.toList())
                }
                matchIdsFlow.flatMapLatest { matchIds ->
                    combine(timelineRepo.feedFor(matchIds), flowOf(followedIds)) { items, ids ->
                        TimelineState(items = items, myUid = uid, myName = user.displayName, isGuest = false, isFollowingAnyone = ids.isNotEmpty())
                    }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimelineState())

    /** A cache-free per-item reaction flow -- the screen calls this once per visible item. */
    fun reactionsFor(item: TimelineItem) = reactionRepo.reactionsFor(item.contentType, item.contentId, state.value.myUid)

    fun commentsFor(item: TimelineItem) = commentRepo.commentsFor(item.contentType, item.contentId)

    fun react(item: TimelineItem, reaction: BiblicalReaction, alreadySelected: Boolean) = viewModelScope.launch {
        val uid = state.value.myUid
        if (uid.isBlank()) return@launch
        if (alreadySelected) reactionRepo.removeReaction(item.contentType, item.contentId, uid)
        else reactionRepo.react(item.contentType, item.contentId, uid, reaction)
    }

    fun addComment(item: TimelineItem, text: String, replyTo: com.gracelink.android.data.repository.TimelineComment? = null) = viewModelScope.launch {
        val s = state.value
        if (s.myUid.isBlank() || text.isBlank()) return@launch
        commentRepo.addComment(
            item.contentType, item.contentId, s.myUid, s.myName, text,
            replyToCommentId = replyTo?.id, replyToAuthorName = replyTo?.authorName,
        )
    }

    /**
     * Resolves what tapping a Timeline item's author name should open --
     * previously there was no way to reach a church or pastor's profile
     * from their own posted content at all, only through Find Churches.
     * An Event's authorId IS the church id already; everything else is
     * authored under the account owner's uid, so that has to be checked
     * against both "is this a church's owner" and "is this a pastor"
     * before deciding where to navigate.
     */
    suspend fun resolveProfileRoute(item: TimelineItem): ProfileRoute? {
        if (item is TimelineItem.Event) return ProfileRoute.Church(item.entity.churchId)
        val authorId = item.authorId
        val church = churchDao.byOwnerOnce(authorId)
        if (church != null) return ProfileRoute.Church(church.id)
        val pastor = registry.getPastor(authorId)
        if (pastor != null) return ProfileRoute.Pastor(pastor.uid)
        return null
    }
}

sealed class ProfileRoute {
    data class Church(val churchId: String) : ProfileRoute()
    data class Pastor(val pastorUid: String) : ProfileRoute()
}
