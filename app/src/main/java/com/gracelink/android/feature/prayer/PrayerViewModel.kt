package com.gracelink.android.feature.prayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.repository.MediaUploadRepository
import com.gracelink.android.data.repository.PrayerFirestoreRepository
import com.gracelink.android.data.repository.PrayerRequest
import com.gracelink.android.data.repository.UserRepository
import com.gracelink.android.player.VoiceRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PrayerTab(val label: String) { ALL("All"), MINE("My Prayers"), ANSWERED("Answered") }

data class PrayerState(
    val tab: PrayerTab = PrayerTab.ALL,
    val allPrayers: List<PrayerRequest> = emptyList(),
    val showSheet: Boolean = false,
    val myName: String = "You",
    val myUid: String = "",
    val isGuest: Boolean = true,
    val recordingPrayerId: String? = null,
    val uploadingPrayerId: String? = null,
) {
    /** Filtered by the currently selected tab -- "mine" and "answered" are
     * computed here against the real signed-in uid, not a stored per-row
     * flag that would be wrong for every viewer except whoever it was
     * hardcoded for. */
    val prayers: List<PrayerRequest> get() = when (tab) {
        PrayerTab.ALL -> allPrayers
        PrayerTab.MINE -> allPrayers.filter { it.authorId == myUid }
        PrayerTab.ANSWERED -> allPrayers.filter { it.isAnswered }
    }
}

private data class PrayerBaseCombined(
    val tab: PrayerTab,
    val allPrayers: List<PrayerRequest>,
    val sheet: Boolean,
    val user: com.gracelink.android.data.db.entity.UserEntity?,
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val repo: PrayerFirestoreRepository,
    private val mediaUpload: MediaUploadRepository,
    @ApplicationContext private val context: Context,
    userRepo: UserRepository,
) : ViewModel() {

    private val tab = MutableStateFlow(PrayerTab.ALL)
    private val showSheet = MutableStateFlow(false)
    private val recordingPrayerId = MutableStateFlow<String?>(null)
    private val uploadingPrayerId = MutableStateFlow<String?>(null)
    private var activeRecorder: VoiceRecorder? = null

    val state: StateFlow<PrayerState> = combine(tab, repo.allPrayers(), showSheet, userRepo.current()) { t, prayers, sheet, user ->
        PrayerBaseCombined(t, prayers, sheet, user)
    }.let { baseFlow ->
        combine(baseFlow, recordingPrayerId, uploadingPrayerId) { base, recording, uploading ->
            PrayerState(
                tab = base.tab,
                allPrayers = base.allPrayers,
                showSheet = base.sheet,
                myName = base.user?.displayName ?: "You",
                myUid = base.user?.uid ?: "",
                isGuest = base.user == null,
                recordingPrayerId = recording,
                uploadingPrayerId = uploading,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerState())

    fun setTab(t: PrayerTab) { tab.value = t }
    fun showSheet(show: Boolean) { showSheet.value = show }

    fun submit(text: String, anonymous: Boolean) = viewModelScope.launch {
        val s = state.value
        if (s.myUid.isBlank() || text.isBlank()) return@launch
        repo.submit(s.myUid, s.myName, text, anonymous)
        showSheet.value = false
    }

    fun togglePrayed(prayer: PrayerRequest) = viewModelScope.launch {
        val uid = state.value.myUid
        if (uid.isBlank()) return@launch
        repo.togglePrayed(prayer.id, uid, currentlyPrayed = uid in prayer.prayedByUids)
    }

    fun markAnswered(prayerId: String) = viewModelScope.launch { repo.markAnswered(prayerId) }

    fun encourage(prayerId: String, text: String) = viewModelScope.launch {
        val s = state.value
        if (s.myUid.isBlank()) return@launch
        repo.addEncouragement(prayerId, s.myUid, s.myName, text)
    }

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

        val s = state.value
        if (s.myUid.isBlank()) return

        uploadingPrayerId.value = prayerId
        viewModelScope.launch {
            try {
                val url = mediaUpload.uploadLocalFile(path, "prayer_replies/$prayerId/${System.currentTimeMillis()}.m4a")
                repo.addEncouragement(prayerId, s.myUid, s.myName, "\uD83C\uDFA4 Voice reply", audioUrl = url)
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
