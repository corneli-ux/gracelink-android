package com.gracelink.android.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.repository.LiveSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repo: LiveSessionRepository,
) : ViewModel() {

    val sessions: StateFlow<List<LiveSessionEntity>> = repo.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleRemind(id: String) = viewModelScope.launch { repo.toggleRemindMe(id) }
    fun toggleQueue(id: String) = viewModelScope.launch { repo.toggleJoinQueue(id) }
}
