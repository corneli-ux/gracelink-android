package com.gracelink.android.feature.audioconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioSpace(
    val id: String,
    val title: String,
    val hostName: String,
    val topic: String,
    val participantCount: Int,
    val isLive: Boolean,
    val startedAt: Long,
)

data class AudioConnectState(
    val spaces: List<AudioSpace> = emptyList(),
    val isCreating: Boolean = false,
    val activeSpace: AudioSpace? = null,
    val isMicOn: Boolean = false,
    val isHandRaised: Boolean = false,
)

@HiltViewModel
class AudioConnectViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AudioConnectState())
    val state: StateFlow<AudioConnectState> = _state.asStateFlow()

    init {
        // Seed with sample live audio spaces
        _state.value = _state.value.copy(
            spaces = listOf(
                AudioSpace("as_001", "Bible Study Discussion", "Pastor Anil Kumar", "Romans 8 — Suffering & Glory", 23, true, System.currentTimeMillis() - 1800000),
                AudioSpace("as_002", "Youth Prayer Room", "Samuel", "Praying for our generation", 8, true, System.currentTimeMillis() - 600000),
                AudioSpace("as_003", "Telugu Worship Circle", "Pas. Raju Venkat", "కీర్తనలు మరియు ప్రార్థన", 15, true, System.currentTimeMillis() - 3600000),
                AudioSpace("as_004", "Theology Q&A", "Dr. Anita", "Predestination vs Free Will", 42, true, System.currentTimeMillis() - 7200000),
                AudioSpace("as_005", "Marriage & Family", "Mark & Lydia", "Raising godly children", 19, true, System.currentTimeMillis() - 900000),
            )
        )
    }

    fun showCreateDialog(show: Boolean) {
        _state.value = _state.value.copy(isCreating = show)
    }

    fun createSpace(title: String, topic: String, myName: String) {
        val space = AudioSpace(
            id = "as_${System.currentTimeMillis()}",
            title = title,
            hostName = myName,
            topic = topic,
            participantCount = 1,
            isLive = true,
            startedAt = System.currentTimeMillis(),
        )
        _state.value = _state.value.copy(
            spaces = listOf(space) + _state.value.spaces,
            isCreating = false,
            activeSpace = space,
        )
    }

    fun joinSpace(space: AudioSpace) {
        _state.value = _state.value.copy(activeSpace = space)
    }

    fun leaveSpace() {
        _state.value = _state.value.copy(activeSpace = null, isMicOn = false, isHandRaised = false)
    }

    fun toggleMic() {
        _state.value = _state.value.copy(isMicOn = !_state.value.isMicOn)
    }

    fun toggleHandRaise() {
        _state.value = _state.value.copy(isHandRaised = !_state.value.isHandRaised)
    }
}
