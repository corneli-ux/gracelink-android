package com.gracelink.android.feature.churchportal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.data.db.entity.ChurchEntity
import com.gracelink.android.data.repository.CloudProfileRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChurchEditProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val churchDao: ChurchDao,
    private val cloudRegistry: CloudProfileRegistry,
) : ViewModel() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val church: StateFlow<ChurchEntity?> = userDao.current().flatMapLatest { user ->
        user?.let { churchDao.byOwner(it.uid) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(
        name: String,
        description: String,
        pastorName: String,
        location: String,
        belief: BeliefSystem,
        website: String,
        phone: String,
        onDone: () -> Unit,
    ) = viewModelScope.launch {
        val current = church.value ?: return@launch
        churchDao.update(
            current.copy(
                name = name,
                description = description,
                pastorName = pastorName,
                location = location,
                beliefSystem = belief,
                website = website.ifBlank { null },
                phone = phone.ifBlank { null },
            )
        )
        // Keep the cloud restoration backup in sync with edits, so a
        // reinstall restores the CURRENT profile, not a stale snapshot
        // from whenever registration originally happened.
        current.ownerUserId?.let { uid ->
            cloudRegistry.writeChurch(
                uid = uid, churchName = name, pastorName = pastorName, location = location,
                beliefSystem = belief, description = description,
                website = website.ifBlank { null }, phone = phone.ifBlank { null },
            )
        }
        onDone()
    }
}
