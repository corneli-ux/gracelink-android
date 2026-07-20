package com.gracelink.android.feature.churchportal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.ChurchMemberDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChurchMembersViewModel @Inject constructor(
    userDao: UserDao,
    churchDao: ChurchDao,
    memberDao: ChurchMemberDao,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val members: StateFlow<List<ChurchMemberEntity>> = userDao.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) flowOf(emptyList())
        else churchDao.byOwner(uid).flatMapLatest { church ->
            if (church == null) flowOf(emptyList()) else memberDao.approvedForChurch(church.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
