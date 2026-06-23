package com.gracelink.android.feature.faith

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.FaithProgressDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.data.db.entity.FaithProgressEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FaithState(
    val progress: FaithProgressEntity? = null,
    val beliefSystem: BeliefSystem = BeliefSystem.NONDENOMINATIONAL,
)

@HiltViewModel
class FaithViewModel @Inject constructor(
    private val progressDao: FaithProgressDao,
    private val userDao: UserDao,
) : ViewModel() {

    val state: StateFlow<FaithState> = userDao.current()
        .flatMapLatest { user ->
            if (user != null) {
                progressDao.forUser(user.uid)
            } else flowOf(null)
        }
        .let { flow ->
            kotlinx.coroutines.flow.combine(flow, userDao.current()) { p, u ->
                FaithState(p, u?.beliefSystem ?: BeliefSystem.NONDENOMINATIONAL)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FaithState())

    init {
        viewModelScope.launch {
            // Initialize faith progress for current user if missing
            val user = userDao.currentOnce()
            if (user != null) {
                val existing = progressDao.getOnce(user.uid)
                if (existing == null) {
                    val now = System.currentTimeMillis()
                    progressDao.upsert(FaithProgressEntity(
                        userId = user.uid,
                        beliefSystem = user.beliefSystem,
                        bibleReadingDays = 0,
                        prayerSessions = 0,
                        churchAttendances = 0,
                        articlesWritten = 0,
                        prayersOffered = user.prayersOffered,
                        membersDiscipled = 0,
                        lastProgressAt = now,
                        sanctificationLevel = 0,
                        gracePeriodEndsAt = now + 30L * 24 * 3600 * 1000, // 30 days grace
                    ))
                }
            }
        }
    }

    fun logBibleReading() = viewModelScope.launch {
        val user = userDao.currentOnce() ?: return@launch
        progressDao.incrementBibleReading(user.uid, System.currentTimeMillis())
    }

    fun logPrayer() = viewModelScope.launch {
        val user = userDao.currentOnce() ?: return@launch
        progressDao.incrementPrayer(user.uid, System.currentTimeMillis())
    }
}
