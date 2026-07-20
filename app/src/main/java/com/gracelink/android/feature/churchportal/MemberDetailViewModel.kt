package com.gracelink.android.feature.churchportal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchMemberDao
import com.gracelink.android.data.db.entity.AdminNoteEntity
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.ChurchRole
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

data class MemberDetailState(
    val member: ChurchMemberEntity? = null,
    val notes: List<AdminNoteEntity> = emptyList(),
    val myUid: String = "",
    val myName: String = "",
)

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    private val memberDao: ChurchMemberDao,
    private val adminRepo: ChurchAdminRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val memberId = MutableStateFlow("")
    private val memberFlow = MutableStateFlow<ChurchMemberEntity?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val notesFlow = memberFlow.flatMapLatest { member ->
        if (member == null) flowOf(emptyList()) else adminRepo.adminNotes(member.churchId, member.userId)
    }

    val state: StateFlow<MemberDetailState> = combine(
        memberFlow, notesFlow, userRepo.current(),
    ) { member, notes, user ->
        MemberDetailState(
            member = member,
            notes = notes,
            myUid = user?.uid ?: "",
            myName = user?.displayName ?: "",
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MemberDetailState())

    fun load(id: String) {
        memberId.value = id
        viewModelScope.launch { memberFlow.value = memberDao.getById(id) }
    }

    fun refresh() = viewModelScope.launch {
        memberFlow.value = memberDao.getById(memberId.value)
    }

    fun setRole(role: ChurchRole) = viewModelScope.launch {
        adminRepo.updateMemberRole(memberId.value, role)
        refresh()
    }

    fun addNote(note: String) = viewModelScope.launch {
        val s = state.value
        if (s.myUid.isBlank() || note.isBlank()) return@launch
        adminRepo.addAdminNote(
            churchId = s.member?.churchId ?: return@launch,
            memberUserId = s.member.userId,
            authorId = s.myUid,
            authorName = s.myName,
            note = note,
        )
    }

    fun removeMember(onDone: () -> Unit) = viewModelScope.launch {
        adminRepo.removeMember(memberId.value)
        onDone()
    }
}
