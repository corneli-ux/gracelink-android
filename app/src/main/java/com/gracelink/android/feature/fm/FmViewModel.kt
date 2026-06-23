package com.gracelink.android.feature.fm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.FmScheduleEntity
import com.gracelink.android.data.repository.FmScheduleRepository
import com.gracelink.android.player.GracePlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FmState(
    val schedule: List<FmScheduleEntity> = emptyList(),
    val currentSlot: FmScheduleEntity? = null,
    val isPlaying: Boolean = false,
    val selectedDay: String = "",
)

@HiltViewModel
class FmViewModel @Inject constructor(
    private val repo: FmScheduleRepository,
    private val playerController: GracePlayerController,
) : ViewModel() {

    private val isPlaying = MutableStateFlow(false)
    private val selectedDay = MutableStateFlow(today())

    val state: StateFlow<FmState> = combine(
        repo.all(), isPlaying, selectedDay
    ) { schedule, playing, day ->
        val current = findCurrentSlot(schedule)
        FmState(schedule, current, playing, day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FmState())

    private val streamUrl = "https://stream.zeno.fm/0r0xa792kwzuv"

    fun togglePlay() = viewModelScope.launch {
        val current = state.value.currentSlot
        if (current != null) {
            // Play the live FM stream
            val content = com.gracelink.android.data.db.entity.ContentEntity(
                id = "fm_live_stream",
                title = "Faith FM Live",
                description = current.preacher + " — " + current.description,
                speaker = current.preacher,
                durationMs = 0,
                audioUrl = streamUrl,
                type = com.gracelink.android.data.db.entity.ContentType.LIVE_RADIO,
                language = com.gracelink.android.data.db.entity.ContentLanguage.EN,
                category = com.gracelink.android.data.db.entity.ContentCategory.WORSHIP,
                thumbnailUrl = null,
                isDownloadable = false,
                publishedAt = 0,
                isLive = true,
                listenerCount = 0,
            )
            if (isPlaying.value) {
                playerController.stop()
                isPlaying.value = false
            } else {
                playerController.play(content)
                isPlaying.value = true
                launch { playerController.pollPosition() }
            }
        }
    }

    fun selectDay(day: String) { selectedDay.value = day }

    companion object {
        fun today(): String {
            val days = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            return days[java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1]
        }

        fun findCurrentSlot(schedule: List<FmScheduleEntity>): FmScheduleEntity? {
            val now = java.util.Calendar.getInstance()
            val today = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")[now.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
            return schedule.firstOrNull { it.day == today && it.startHour <= currentHour && currentHour < it.startHour + 2 }
        }
    }
}
