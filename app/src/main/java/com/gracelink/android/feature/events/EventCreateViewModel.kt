package com.gracelink.android.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.UserEntity
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventCreateViewModel @Inject constructor(
    private val eventDao: ChurchEventDao,
    private val churchDao: ChurchDao,
    private val userRepo: UserRepository,
) : ViewModel() {

    val me: StateFlow<UserEntity?> = userRepo.current()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Resolves the user directly via a one-shot suspend query rather than
     * trusting me.value, which could still be null if tapped before this
     * screen's first real state emission arrives. */
    fun createEvent(
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
        isOnline: Boolean,
        meetingLink: String?,
        location: String?,
        category: String,
        onDone: () -> Unit,
    ) = viewModelScope.launch {
        val user = userRepo.currentOnce() ?: return@launch
        // Churches get a real church record as the host; individual pastors
        // host under their own profile id/name (no church backing needed).
        val church = churchDao.byOwnerOnce(user.uid)
        val hostId = church?.id ?: user.uid
        val hostName = church?.name ?: user.displayName

        eventDao.insert(
            ChurchEventEntity(
                id = "evt_${System.currentTimeMillis()}",
                churchId = hostId,
                churchName = hostName,
                title = title,
                description = description,
                startTime = startTime,
                endTime = endTime,
                isOnline = isOnline,
                meetingLink = meetingLink,
                location = location,
                category = category,
                attendeeCount = 0,
            )
        )
        onDone()
    }
}
