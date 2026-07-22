package com.gracelink.android.feature.servicetimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ServiceTimeEntity
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

data class ServiceTimesState(
    val serviceTimes: List<ServiceTimeEntity> = emptyList(),
    val churchId: String? = null,
)

@HiltViewModel
class ServiceTimesViewModel @Inject constructor(
    private val repo: ChurchAdminRepository,
    private val churchDao: ChurchDao,
    private val userDao: UserDao,
) : ViewModel() {

    private val currentChurchId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ServiceTimesState> = userDao.current().flatMapLatest { user ->
        val uid = user?.uid
        if (uid == null) {
            currentChurchId.value = null
            flowOf(ServiceTimesState())
        } else {
            churchDao.byOwner(uid).flatMapLatest { church ->
                currentChurchId.value = church?.id
                if (church == null) flowOf(ServiceTimesState())
                else repo.serviceTimes(church.id).map { times -> ServiceTimesState(serviceTimes = times, churchId = church.id) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ServiceTimesState())

    /**
     * Resolves the church via a direct one-shot suspend query rather than
     * trusting currentChurchId.value -- see MinistriesViewModel for the
     * full explanation, same bug class, same fix.
     */
    fun addServiceTime(dayOfWeek: Int, time: String, name: String, location: String, isOnline: Boolean, onDone: () -> Unit) = viewModelScope.launch {
        val uid = userDao.currentOnce()?.uid ?: return@launch
        val churchId = churchDao.byOwnerOnce(uid)?.id ?: return@launch
        repo.addServiceTime(
            churchId = churchId, dayOfWeek = dayOfWeek, time = time, name = name,
            location = location.ifBlank { null }, isOnline = isOnline,
        )
        onDone()
    }
}
