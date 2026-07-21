package com.gracelink.android.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.repository.BiblicalReaction
import com.gracelink.android.data.repository.FollowRepository
import com.gracelink.android.data.repository.ReactionRepository
import com.gracelink.android.data.repository.TimelineComment
import com.gracelink.android.data.repository.TimelineCommentRepository
import com.gracelink.android.data.repository.TimelineItem
import com.gracelink.android.data.repository.TimelineRepository
import com.gracelink.android.data.repository.UserRepository
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
    private val userRepo: UserRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TimelineState> = userRepo.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) {
            flowOf(TimelineState())
        } else {
            followRepo.followedIdsFor(uid).flatMapLatest { followedIds ->
                timelineRepo.feedFor(followedIds).let { feedFlow ->
                    combine(feedFlow, flowOf(followedIds)) { items, ids ->
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

    fun addComment(item: TimelineItem, text: String) = viewModelScope.launch {
        val s = state.value
        if (s.myUid.isBlank() || text.isBlank()) return@launch
        commentRepo.addComment(item.contentType, item.contentId, s.myUid, s.myName, text)
    }
}
