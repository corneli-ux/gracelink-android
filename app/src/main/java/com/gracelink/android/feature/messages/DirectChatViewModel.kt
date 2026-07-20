package com.gracelink.android.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.repository.DirectMessage
import com.gracelink.android.data.repository.DirectMessageRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DirectChatState(
    val messages: List<DirectMessage> = emptyList(),
    val myUid: String = "",
    val myName: String = "",
)

@HiltViewModel
class DirectChatViewModel @Inject constructor(
    private val repo: DirectMessageRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val otherUserId = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val messagesFlow = combine(otherUserId, userRepo.current()) { other, user -> other to user?.uid }
        .flatMapLatest { (other, myUid) ->
            if (other.isBlank() || myUid.isNullOrBlank()) flowOf(emptyList())
            else repo.conversation(myUid, other)
        }

    val state: StateFlow<DirectChatState> = combine(messagesFlow, userRepo.current()) { messages, user ->
        DirectChatState(messages = messages, myUid = user?.uid ?: "", myName = user?.displayName ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DirectChatState())

    fun load(otherUid: String) {
        otherUserId.value = otherUid
    }

    fun send(text: String) = viewModelScope.launch {
        val s = state.value
        val other = otherUserId.value
        if (s.myUid.isBlank() || other.isBlank() || text.isBlank()) return@launch
        repo.send(s.myUid, s.myName, other, text)
    }
}
