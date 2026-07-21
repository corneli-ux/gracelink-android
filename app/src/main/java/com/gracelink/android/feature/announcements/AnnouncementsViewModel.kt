package com.gracelink.android.feature.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.ChurchDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.AnnouncementPriority
import com.gracelink.android.data.repository.ChurchAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val repo: ChurchAdminRepository,
    private val churchDao: ChurchDao,
    private val userDao: UserDao,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val announcements = userDao.current()
        .flatMapLatest { user ->
            val uid = user?.uid
            if (uid == null) {
                flowOf(emptyList())
            } else {
                churchDao.byOwner(uid).flatMapLatest { church ->
                    if (church == null) flowOf(emptyList()) else repo.announcements(church.id)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Resolves the church/author directly via a one-shot suspend query
     * rather than reading a cached value from [announcements]'s collection
     * side effects. That StateFlow only updates once something actually
     * collects it (WhileSubscribed) -- on a create-only screen that never
     * displays the list, nothing ever does, so the cached values stayed
     * null forever and create() silently no-op'd, leaving the "Publish"
     * button spinning forever since its onDone callback never fired.
     */
    fun create(title: String, body: String, priority: AnnouncementPriority, onDone: () -> Unit) = viewModelScope.launch {
        val user = userDao.currentOnce() ?: return@launch
        val church = churchDao.byOwnerOnce(user.uid) ?: return@launch
        repo.createAnnouncement(
            churchId = church.id,
            authorId = user.uid,
            authorName = user.displayName,
            title = title,
            body = body,
            priority = priority,
        )
        onDone()
    }
}
