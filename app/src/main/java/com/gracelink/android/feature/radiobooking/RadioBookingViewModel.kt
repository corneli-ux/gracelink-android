package com.gracelink.android.feature.radiobooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.entity.FmScheduleEntity
import com.gracelink.android.data.db.entity.PodcastEpisodeEntity
import com.gracelink.android.data.db.entity.UserEntity
import com.gracelink.android.data.repository.FmScheduleRepository
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

data class RadioBookingState(
    val me: UserEntity? = null,
    val selectedDay: String = "MON",
    val daySlots: List<FmScheduleEntity> = emptyList(),
    val myBookings: List<FmScheduleEntity> = emptyList(),
    val myEpisodes: List<PodcastEpisodeEntity> = emptyList(),
    val message: String? = null,
)

@HiltViewModel
class RadioBookingViewModel @Inject constructor(
    private val repo: FmScheduleRepository,
    private val podcastDao: PodcastDao,
    private val userRepo: UserRepository,
) : ViewModel() {

    private val selectedDay = MutableStateFlow("MON")
    private val message = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val myEpisodesFlow = userRepo.current().flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else podcastDao.seriesByAuthor(user.uid).flatMapLatest { seriesList ->
            if (seriesList.isEmpty()) flowOf(emptyList())
            else combine(seriesList.map { podcastDao.episodesFor(it.id) }) { arrays -> arrays.toList().flatten() }
        }
    }

    val state: StateFlow<RadioBookingState> = combine(
        userRepo.current(), selectedDay, message, repo.all(), myEpisodesFlow,
    ) { user, day, msg, all, episodes ->
        RadioBookingState(
            me = user,
            selectedDay = day,
            daySlots = all.filter { it.day == day }.sortedBy { it.startHour },
            myBookings = user?.let { u -> all.filter { it.bookedByUid == u.uid } } ?: emptyList(),
            myEpisodes = episodes,
            message = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RadioBookingState())

    fun selectDay(day: String) { selectedDay.value = day }

    fun bookSlot(slotId: String) = viewModelScope.launch {
        val user = userRepo.currentOnce()
        if (user == null) {
            message.value = "Set up your profile first"
            return@launch
        }
        val ok = repo.bookSlot(slotId, user.uid, user.displayName)
        message.value = if (ok) "Slot booked" else "Someone just booked this slot"
    }

    fun cancelBooking(slotId: String) = viewModelScope.launch {
        val user = userRepo.currentOnce() ?: return@launch
        repo.cancelBooking(slotId, user.uid)
        message.value = "Booking cancelled"
    }

    fun attachContent(slotId: String, episode: PodcastEpisodeEntity) = viewModelScope.launch {
        val user = userRepo.currentOnce() ?: return@launch
        repo.attachContent(slotId, user.uid, episode.id, episode.title)
        message.value = "Episode attached to slot"
    }

    fun clearMessage() { message.value = null }
}
