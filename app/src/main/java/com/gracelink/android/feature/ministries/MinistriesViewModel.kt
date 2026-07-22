package com.gracelink.android.feature.ministries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.MinistryEntity
import com.gracelink.android.data.repository.ChurchAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MinistriesState(
    val ministries: List<MinistryEntity> = emptyList(),
    val churchId: String? = null,
)

@HiltViewModel
class MinistriesViewModel @Inject constructor(
    private val repo: ChurchAdminRepository,
    private val churchDao: ChurchDao,
    private val userDao: UserDao,
) : ViewModel() {

    private val currentChurchId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MinistriesState> = userDao.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) {
            currentChurchId.value = null
            flowOf(MinistriesState())
        } else {
            churchDao.byOwner(uid).flatMapLatest { church ->
                currentChurchId.value = church?.id
                if (church == null) flowOf(MinistriesState())
                else repo.ministries(church.id).map { ministries -> MinistriesState(ministries = ministries, churchId = church.id) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MinistriesState())

    /**
     * Resolves the church via a direct one-shot suspend query rather than
     * trusting currentChurchId.value, which is only populated as a side
     * effect of state's flatMapLatest chain resolving -- if createMinistry
     * is tapped before that async user->church lookup completes, the
     * cached value is still null and this would previously return early
     * with zero feedback (same bug class already found and fixed in
     * Announcements/Groups/Forum's create flows).
     */
    fun createMinistry(name: String, description: String, meetingInfo: String, onDone: () -> Unit) = viewModelScope.launch {
        val uid = userDao.currentOnce()?.uid ?: return@launch
        val churchId = churchDao.byOwnerOnce(uid)?.id ?: return@launch
        repo.createMinistry(
            churchId = churchId, name = name, description = description,
            meetingInfo = meetingInfo.ifBlank { null },
        )
        onDone()
    }
}
