package com.gracelink.android.feature.fm

import androidx.lifecycle.ViewModel
import com.gracelink.android.data.db.entity.FmScheduleEntity
import com.gracelink.android.data.repository.FmScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class FmViewModel @Inject constructor(
    private val repo: FmScheduleRepository,
) : ViewModel() {

    val schedule: StateFlow<List<FmScheduleEntity>> = repo.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun today(): String {
            val days = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            return days[java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1]
        }
    }
}
