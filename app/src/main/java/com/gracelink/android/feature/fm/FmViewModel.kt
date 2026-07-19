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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedDay: String = "",
)

private const val FM_STREAM_CONTENT_ID = "fm_live_stream"

@HiltViewModel
class FmViewModel @Inject constructor(
    private val repo: FmScheduleRepository,
    private val playerController: GracePlayerController,
) : ViewModel() {

    private val selectedDay = MutableStateFlow(today())
    private var pollJob: kotlinx.coroutines.Job? = null

    // BUG FIX: the previous version tracked its own local `isPlaying` flag
    // that was set on tap and never corrected against what ExoPlayer was
    // actually doing. If the stream failed to buffer, got interrupted, or
    // was paused from the system notification, this screen would keep
    // showing "playing" with no audio -- looking broken with no way to
    // tell why. State now comes directly from the real player.
    val state: StateFlow<FmState> = combine(
        repo.all(), playerController.state, selectedDay
    ) { schedule, playerState, day ->
        val current = findCurrentSlot(schedule)
        val isThisStream = playerState.current?.id == FM_STREAM_CONTENT_ID
        FmState(
            schedule = schedule,
            currentSlot = current,
            isPlaying = isThisStream && playerState.isPlaying,
            isLoading = isThisStream && playerState.isLoading,
            errorMessage = if (isThisStream) playerState.errorMessage else null,
            selectedDay = day,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FmState())

    private val streamUrl = "https://stream.zeno.fm/0r0xa792kwzuv"

    fun togglePlay() {
        val current = state.value.currentSlot ?: return
        val playerState = playerController.state.value
        val isThisStreamLoaded = playerState.current?.id == FM_STREAM_CONTENT_ID

        if (isThisStreamLoaded) {
            // Already loaded (playing or paused) -- just toggle, don't
            // reload the stream from scratch every tap.
            playerController.togglePlayPause()
        } else {
            val content = com.gracelink.android.data.db.entity.ContentEntity(
                id = FM_STREAM_CONTENT_ID,
                title = "GraceLink Radio Live",
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
            playerController.play(content)
            pollJob?.cancel()
            pollJob = viewModelScope.launch { playerController.pollPosition() }
        }
    }

    fun selectDay(day: String) { selectedDay.value = day }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }

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
