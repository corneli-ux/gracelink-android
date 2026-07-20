package com.gracelink.android.feature.radiobooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.FmScheduleEntity
import com.gracelink.android.data.db.entity.UserEntity
import com.gracelink.android.data.repository.FmScheduleRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RadioBookingState(
    val me: UserEntity? = null,
    val selectedDay: String = "MON",
    val daySlots: List<FmScheduleEntity> = emptyList(),
    val myBookings: List<FmScheduleEntity> = emptyList(),
    val message: String? = null,
)

@HiltViewModel
class RadioBookingViewModel @Inject constructor(
    private val repo: FmScheduleRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    private val selectedDay = MutableStateFlow("MON")
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<RadioBookingState> = combine(
        userRepo.current(), selectedDay, message, repo.all()
    ) { user, day, msg, all ->
        RadioBookingState(
            me = user,
            selectedDay = day,
            daySlots = all.filter { it.day == day }.sortedBy { it.startHour },
            myBookings = user?.let { u -> all.filter { it.bookedByUid == u.uid } } ?: emptyList(),
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

    fun clearMessage() { message.value = null }
}
