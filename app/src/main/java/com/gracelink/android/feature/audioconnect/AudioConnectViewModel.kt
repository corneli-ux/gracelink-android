package com.gracelink.android.feature.audioconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.repository.LiveSpaceRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * All fields default so Firestore's automatic POJO mapping (which needs
 * a public no-arg constructor) works -- Kotlin data classes only get one
 * automatically when every parameter has a default.
 */
data class AudioSpace(
    val id: String = "",
    val title: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val topic: String = "",
    val participantCount: Int = 0,
    val isLive: Boolean = true,
    val startedAt: Long = 0,
)

data class AudioConnectState(
    val spaces: List<AudioSpace> = emptyList(),
    val isCreating: Boolean = false,
    val activeSpace: AudioSpace? = null,
    val isMicOn: Boolean = false,
    val isHandRaised: Boolean = false,
    val createError: String? = null,
)

@HiltViewModel
class AudioConnectViewModel @Inject constructor(
    private val repo: LiveSpaceRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    private val isCreating = MutableStateFlow(false)
    private val activeSpaceId = MutableStateFlow<String?>(null)
    private val isMicOn = MutableStateFlow(false)
    private val isHandRaised = MutableStateFlow(false)
    private val createError = MutableStateFlow<String?>(null)

    val state: StateFlow<AudioConnectState> = combine(
        repo.activeSpaces(), isCreating, activeSpaceId, isMicOn, isHandRaised,
    ) { spaces, creating, activeId, mic, hand ->
        AudioConnectState(
            spaces = spaces,
            isCreating = creating,
            activeSpace = spaces.firstOrNull { it.id == activeId },
            isMicOn = mic,
            isHandRaised = hand,
        )
    }.let { baseFlow ->
        combine(baseFlow, createError) { base, error -> base.copy(createError = error) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudioConnectState())

    fun showCreateDialog(show: Boolean) {
        isCreating.value = show
        if (show) createError.value = null
    }

    /**
     * Previously this had no error handling at all -- if the Firestore
     * write failed for any reason (network, security rules, anything),
     * the exception was silently swallowed by the coroutine and
     * isCreating never got reset, leaving the dialog open with no
     * feedback and no space ever actually going live. Now failures
     * surface a real message instead of just doing nothing.
     */
    fun createSpace(title: String, topic: String) = viewModelScope.launch {
        createError.value = null
        try {
            val user = userRepo.currentOnce() ?: run {
                createError.value = "You need to be signed in to go live."
                return@launch
            }
            if (title.isBlank()) {
                createError.value = "Give your space a title first."
                return@launch
            }
            val id = repo.createSpace(title, topic, user.uid, user.displayName)
            activeSpaceId.value = id
            isCreating.value = false
        } catch (e: Exception) {
            createError.value = "Couldn't go live: ${e.message ?: "please check your connection and try again"}"
        }
    }

    fun joinSpace(space: AudioSpace) = viewModelScope.launch {
        repo.joinSpace(space.id)
        activeSpaceId.value = space.id
    }

    fun leaveSpace() = viewModelScope.launch {
        activeSpaceId.value?.let { repo.leaveSpace(it) }
        activeSpaceId.value = null
        isMicOn.value = false
        isHandRaised.value = false
    }

    fun toggleMic() { isMicOn.value = !isMicOn.value }
    fun toggleHandRaise() { isHandRaised.value = !isHandRaised.value }
}
