package com.gracelink.android.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.EventRsvpEntity
import com.gracelink.android.data.db.entity.RsvpStatus
import com.gracelink.android.data.repository.ChurchAdminRepository
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

data class EventRsvpState(
    val event: ChurchEventEntity? = null,
    val rsvps: List<EventRsvpEntity> = emptyList(),
    val myUid: String = "",
    val myName: String = "",
) {
    val myStatus: RsvpStatus? get() = rsvps.firstOrNull { it.userId == myUid }?.status
    val goingCount: Int get() = rsvps.count { it.status == RsvpStatus.GOING }
    val maybeCount: Int get() = rsvps.count { it.status == RsvpStatus.MAYBE }
}

@HiltViewModel
class EventRsvpViewModel @Inject constructor(
    private val repo: ChurchAdminRepository,
    private val eventDao: ChurchEventDao,
    userRepo: UserRepository,
) : ViewModel() {

    private val eventId = MutableStateFlow("")
    private val eventFlow = MutableStateFlow<ChurchEventEntity?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rsvpsFlow = eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else repo.rsvps(id)
    }

    val state: StateFlow<EventRsvpState> = combine(eventFlow, rsvpsFlow, userRepo.current()) { event, rsvps, user ->
        EventRsvpState(
            event = event,
            rsvps = rsvps,
            myUid = user?.uid ?: "",
            myName = user?.displayName ?: "",
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventRsvpState())

    fun load(id: String) {
        eventId.value = id
        viewModelScope.launch { eventFlow.value = eventDao.getById(id) }
    }

    fun setRsvp(status: RsvpStatus) = viewModelScope.launch {
        val s = state.value
        val event = s.event ?: return@launch
        if (s.myUid.isBlank()) return@launch
        repo.setRsvp(event.id, event.churchId, s.myUid, s.myName, status)
    }
}
