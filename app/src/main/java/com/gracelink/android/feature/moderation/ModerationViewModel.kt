package com.gracelink.android.feature.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ModerationActionEntity
import com.gracelink.android.data.repository.ChurchAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val repo: ChurchAdminRepository,
    private val churchDao: ChurchDao,
    private val userDao: UserDao,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val actions: StateFlow<List<ModerationActionEntity>> = userDao.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) flowOf(emptyList())
        else churchDao.byOwner(uid).flatMapLatest { church ->
            if (church == null) flowOf(emptyList()) else repo.moderationLog(church.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
