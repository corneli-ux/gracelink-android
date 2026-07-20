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

    // Cached so create() doesn't need to re-resolve the church separately.
    private val currentChurchId = MutableStateFlow<String?>(null)
    private val currentAuthor = MutableStateFlow<Pair<String, String>?>(null) // uid to name

    @OptIn(ExperimentalCoroutinesApi::class)
    val announcements = userDao.current()
        .flatMapLatest { user ->
            val uid = user?.uid
            currentAuthor.value = if (user != null) user.uid to user.displayName else null
            if (uid == null) {
                currentChurchId.value = null
                flowOf(emptyList())
            } else {
                churchDao.byOwner(uid).flatMapLatest { church ->
                    currentChurchId.value = church?.id
                    if (church == null) flowOf(emptyList()) else repo.announcements(church.id)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun create(title: String, body: String, priority: AnnouncementPriority, onDone: () -> Unit) {
        val churchId = currentChurchId.value ?: return
        val author = currentAuthor.value ?: return
        viewModelScope.launch {
            repo.createAnnouncement(
                churchId = churchId,
                authorId = author.first,
                authorName = author.second,
                title = title,
                body = body,
                priority = priority,
            )
            onDone()
        }
    }
}
