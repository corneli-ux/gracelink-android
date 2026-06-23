package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.LiveSessionDao
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.db.entity.LiveSessionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveSessionRepository @Inject constructor(
    private val dao: LiveSessionDao,
) {
    fun all(): Flow<List<LiveSessionEntity>> = dao.all()
    fun byStatus(status: LiveSessionStatus): Flow<List<LiveSessionEntity>> = dao.byStatus(status)
    suspend fun getById(id: String): LiveSessionEntity? = dao.getById(id)

    suspend fun toggleRemindMe(id: String) {
        val s = dao.getById(id) ?: return
        dao.update(s.copy(remindMe = !s.remindMe))
    }

    suspend fun toggleJoinQueue(id: String) {
        val s = dao.getById(id) ?: return
        dao.update(s.copy(joinedQueue = !s.joinedQueue))
    }
}
