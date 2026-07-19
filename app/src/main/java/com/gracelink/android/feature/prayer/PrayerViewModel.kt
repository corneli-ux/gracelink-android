package com.gracelink.android.feature.prayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.PrayerEntity
import com.gracelink.android.data.repository.MediaUploadRepository
import com.gracelink.android.data.repository.PrayerRepository
import com.gracelink.android.data.repository.UserRepository
import com.gracelink.android.player.VoiceRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PrayerTab(val label: String) { ALL("All"), MINE("My Prayers"), ANSWERED("Answered") }

data class PrayerState(
    val tab: PrayerTab = PrayerTab.ALL,
    val prayers: List<PrayerEntity> = emptyList(),
    val showSheet: Boolean = false,
    val myName: String = "You",
    val isGuest: Boolean = true,
    val recordingPrayerId: String? = null,
    val uploadingPrayerId: String? = null,
)

private data class PrayerBaseCombined(
    val tab: PrayerTab,
    val prayers: List<PrayerEntity>,
    val sheet: Boolean,
    val user: com.gracelink.android.data.db.entity.UserEntity?,
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val repo: PrayerRepository,
    private val mediaUpload: MediaUploadRepository,
    @ApplicationContext private val context: Context,
    userRepo: UserRepository,
) : ViewModel() {

    private val tab = MutableStateFlow(PrayerTab.ALL)
    private val showSheet = MutableStateFlow(false)
    private val recordingPrayerId = MutableStateFlow<String?>(null)
    private val uploadingPrayerId = MutableStateFlow<String?>(null)
    private var activeRecorder: VoiceRecorder? = null

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val prayersFlow = tab.flatMapLatest { t ->
        when (t) {
            PrayerTab.ALL -> repo.approved()
            PrayerTab.MINE -> repo.mine()
            PrayerTab.ANSWERED -> repo.answered()
        }
    }

    val state: StateFlow<PrayerState> = combine(tab, prayersFlow, showSheet, userRepo.current()) { t, prayers, sheet, user ->
        PrayerBaseCombined(t, prayers, sheet, user)
    }.let { baseFlow ->
        combine(baseFlow, recordingPrayerId, uploadingPrayerId) { base, recording, uploading ->
            PrayerState(
                tab = base.tab,
                prayers = base.prayers,
                showSheet = base.sheet,
                myName = base.user?.displayName ?: "You",
                isGuest = base.user == null,
                recordingPrayerId = recording,
                uploadingPrayerId = uploading,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerState())

    fun setTab(t: PrayerTab) { tab.value = t }
    fun showSheet(show: Boolean) { showSheet.value = show }
    fun submit(text: String, anonymous: Boolean) = viewModelScope.launch {
        repo.submit(text, anonymous, state.value.myName)
        showSheet.value = false
    }
    fun togglePrayed(id: String) = viewModelScope.launch { repo.togglePrayed(id) }
    fun markAnswered(id: String) = viewModelScope.launch { repo.markAnswered(id) }
    fun encourage(id: String, text: String) = viewModelScope.launch { repo.addEncouragement(id, text, state.value.myName) }

    /** Starts recording a voice reply for the given prayer. Caller must have RECORD_AUDIO granted. */
    fun startRecording(prayerId: String) {
        if (recordingPrayerId.value != null) return
        try {
            val recorder = VoiceRecorder(context)
            recorder.start()
            activeRecorder = recorder
            recordingPrayerId.value = prayerId
        } catch (_: Exception) {
            activeRecorder = null
            recordingPrayerId.value = null
        }
    }

    /** Stops recording, uploads the clip, and attaches it as an encouragement. */
    fun stopRecordingAndSend(prayerId: String) {
        val recorder = activeRecorder ?: return
        val path = recorder.stop()
        activeRecorder = null
        recordingPrayerId.value = null
        if (path == null) return

        uploadingPrayerId.value = prayerId
        viewModelScope.launch {
            try {
                val url = mediaUpload.uploadLocalFile(path, "prayer_replies/$prayerId/${System.currentTimeMillis()}.m4a")
                repo.addEncouragement(prayerId, "\uD83C\uDFA4 Voice reply", state.value.myName, audioUrl = url)
            } catch (_: Exception) {
                // Upload failed silently for now -- the recording is still on device
                // at `path` if we want to add a retry affordance later.
            } finally {
                uploadingPrayerId.value = null
            }
        }
    }

    fun cancelRecording() {
        activeRecorder?.cancel()
        activeRecorder = null
        recordingPrayerId.value = null
    }
}
