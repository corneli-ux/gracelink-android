package com.gracelink.android.feature.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.PrayerEntity
import com.gracelink.android.data.repository.PrayerRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val repo: PrayerRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val tab = MutableStateFlow(PrayerTab.ALL)
    private val showSheet = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val prayersFlow = tab.flatMapLatest { t ->
        when (t) {
            PrayerTab.ALL -> repo.approved()
            PrayerTab.MINE -> repo.mine()
            PrayerTab.ANSWERED -> repo.answered()
        }
    }

    val state: StateFlow<PrayerState> = combine(tab, prayersFlow, showSheet, userRepo.current()) { t, prayers, sheet, user ->
        PrayerState(t, prayers, sheet, user?.displayName ?: "You", isGuest = user == null)
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
}
