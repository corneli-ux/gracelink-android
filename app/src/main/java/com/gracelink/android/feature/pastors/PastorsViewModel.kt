package com.gracelink.android.feature.pastors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.repository.CloudProfileRegistry
import com.gracelink.android.data.repository.PastorProfile
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PastorsState(
    val pastors: List<PastorProfile> = emptyList(),
    val isGuest: Boolean = true,
)

@HiltViewModel
class PastorsViewModel @Inject constructor(
    private val registry: CloudProfileRegistry,
    userRepo: UserRepository,
) : ViewModel() {

    val state: StateFlow<PastorsState> = combine(registry.allPastors(), userRepo.current()) { pastors, user ->
        PastorsState(pastors = pastors, isGuest = user == null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PastorsState())
}
