package com.gracelink.android.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.HomeData
import com.gracelink.android.data.repository.LiveSessionRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "Shalom",
    val userName: String = "",
    val home: HomeData = HomeData(emptyList(), emptyList(), emptyList()),
    val liveSessionId: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val liveRepository: LiveSessionRepository,
    userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        // Greet the user by name + pull home data + find a live session if any.
        viewModelScope.launch {
            val user = userRepository.user.value
            val home = contentRepository.fetchHome()
            val liveNow = liveRepository.getByStatus(
                com.gracelink.android.data.model.LiveSessionStatus.LIVE
            ).firstOrNull()
            _state.value = HomeUiState(
                isLoading = false,
                greeting = greetingFor(),
                userName = user.displayName,
                home = home,
                liveSessionId = liveNow?.id,
            )
        }
    }

    private fun greetingFor(): String {
        val hour = java.time.LocalTime.now().hour
        return when (hour) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else      -> "Peace be with you"
        }
    }
}
