package com.gracelink.android.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.GroupType
import com.gracelink.android.data.repository.ChurchAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val repo: ChurchAdminRepository,
    private val churchDao: ChurchDao,
    private val userDao: UserDao,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val groups = userDao.current()
        .flatMapLatest { user ->
            val uid = user?.uid
            if (uid == null) {
                flowOf(emptyList())
            } else {
                churchDao.byOwner(uid).flatMapLatest { church ->
                    if (church == null) flowOf(emptyList()) else repo.groups(church.id)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** See AnnouncementsViewModel.create() for why this resolves the
     * church directly rather than reading a cached collection side effect. */
    fun createGroup(name: String, description: String, type: GroupType, isPrivate: Boolean, onDone: (String) -> Unit) = viewModelScope.launch {
        val user = userDao.currentOnce() ?: return@launch
        val church = churchDao.byOwnerOnce(user.uid) ?: return@launch
        val id = repo.createGroup(
            churchId = church.id, name = name, description = description,
            type = type, leaderUserId = null, leaderName = null, isPrivate = isPrivate,
        )
        onDone(id)
    }
}
