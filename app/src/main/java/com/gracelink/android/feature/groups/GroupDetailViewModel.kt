package com.gracelink.android.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.ChurchGroupEntity
import com.gracelink.android.data.db.entity.GroupMemberEntity
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

data class GroupDetailState(
    val group: ChurchGroupEntity? = null,
    val members: List<GroupMemberEntity> = emptyList(),
    val myUid: String = "",
    val myName: String = "",
    val isMember: Boolean = false,
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val repo: ChurchAdminRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val groupId = MutableStateFlow("")
    private val groupFlow = MutableStateFlow<ChurchGroupEntity?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val membersFlow = groupId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else repo.groupMembers(id)
    }

    val state: StateFlow<GroupDetailState> = combine(groupFlow, membersFlow, userRepo.current()) { group, members, user ->
        GroupDetailState(
            group = group,
            members = members,
            myUid = user?.uid ?: "",
            myName = user?.displayName ?: "",
            isMember = user != null && members.any { it.userId == user.uid },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupDetailState())

    fun load(id: String) {
        groupId.value = id
        viewModelScope.launch { groupFlow.value = repo.getGroup(id) }
    }

    fun joinGroup() = viewModelScope.launch {
        val s = state.value
        val group = s.group ?: return@launch
        if (s.myUid.isBlank() || s.isMember) return@launch
        repo.addToGroup(group.id, group.churchId, s.myUid, s.myName)
    }

    fun leaveGroup() = viewModelScope.launch {
        val s = state.value
        val group = s.group ?: return@launch
        if (s.myUid.isBlank()) return@launch
        repo.removeFromGroup(group.id, s.myUid)
    }
}
