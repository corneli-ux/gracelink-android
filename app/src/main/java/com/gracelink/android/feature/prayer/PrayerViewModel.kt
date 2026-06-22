package com.gracelink.android.feature.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.model.PrayerRequest
import com.gracelink.android.data.repository.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PrayerTab(val label: String) {
    ALL("All"),
    MINE("My Prayers"),
    ANSWERED("Answered")
}

data class PrayerUiState(
    val isLoading: Boolean = false,
    val prayers: List<PrayerRequest> = emptyList(),
    val activeTab: PrayerTab = PrayerTab.ALL,
    val showSubmitSheet: Boolean = false,
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val repository: PrayerRepository,
) : ViewModel() {

    private val _activeTab = MutableStateFlow(PrayerTab.ALL)
    private val _showSheet = MutableStateFlow(false)

    val state: StateFlow<PrayerUiState> = combine(
        repository.prayers,
        _activeTab,
        _showSheet,
    ) { prayers, tab, sheet ->
        val filtered = when (tab) {
            PrayerTab.ALL -> prayers
            PrayerTab.MINE -> prayers.filter { it.isMine }
            PrayerTab.ANSWERED -> prayers.filter { it.isAnswered }
        }
        PrayerUiState(false, filtered, tab, sheet)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerUiState())

    init { refresh() }

    fun setTab(tab: PrayerTab) { _activeTab.value = tab }
    fun showSubmitSheet(show: Boolean) { _showSheet.value = show }
    fun submitPrayer(text: String, anonymous: Boolean) {
        repository.submitPrayer(text, anonymous)
        _showSheet.value = false
    }
    fun togglePrayed(id: String) = repository.togglePrayed(id)
    fun markAnswered(id: String) = repository.markAnswered(id)
    fun addEncouragement(id: String, text: String) = repository.addEncouragement(id, text)

    private fun refresh() {
        viewModelScope.launch { repository.fetchPrayers() }
    }
}
