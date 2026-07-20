package com.gracelink.android.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.ChurchGroupEntity
import com.gracelink.android.data.db.entity.GroupMessageEntity
import com.gracelink.android.data.repository.ChurchAdminRepository
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

data class GroupChatState(
    val group: ChurchGroupEntity? = null,
    val messages: List<GroupMessageEntity> = emptyList(),
    val myUid: String = "",
    val myName: String = "",
)

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val repo: ChurchAdminRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val groupId = MutableStateFlow("")
    private val groupFlow = MutableStateFlow<ChurchGroupEntity?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val messagesFlow = groupId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else repo.groupMessages(id)
    }

    val state: StateFlow<GroupChatState> = combine(groupFlow, messagesFlow, userRepo.current()) { group, messages, user ->
        GroupChatState(
            group = group,
            messages = messages,
            myUid = user?.uid ?: "",
            myName = user?.displayName ?: "",
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupChatState())

    fun load(id: String) {
        groupId.value = id
        viewModelScope.launch { groupFlow.value = repo.getGroup(id) }
    }

    fun send(text: String) = viewModelScope.launch {
        val s = state.value
        val group = s.group ?: return@launch
        if (s.myUid.isBlank() || text.isBlank()) return@launch
        repo.sendGroupMessage(group.id, group.churchId, s.myUid, s.myName, text)
    }
}
