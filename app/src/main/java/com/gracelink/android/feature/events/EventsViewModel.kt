package com.gracelink.android.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.model.LiveSession
import com.gracelink.android.data.repository.LiveSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventsUiState(
    val sessions: List<LiveSession> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val liveRepository: LiveSessionRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val state: StateFlow<EventsUiState> = combine(
        liveRepository.sessions,
        _isLoading,
    ) { sessions, loading ->
        EventsUiState(sessions = sessions, isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventsUiState())

    init { refresh() }

    fun toggleRemindMe(id: String) = liveRepository.toggleRemindMe(id)
    fun toggleJoinQueue(id: String) = liveRepository.toggleJoinQueue(id)

    private fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            liveRepository.fetchSessions()
            _isLoading.value = false
        }
    }
}
