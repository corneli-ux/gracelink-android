package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.FmScheduleDao
import com.gracelink.android.data.db.entity.FmScheduleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FmScheduleRepository @Inject constructor(
    private val dao: FmScheduleDao,
) {
    fun all(): Flow<List<FmScheduleEntity>> = dao.all()
    fun forDay(day: String): Flow<List<FmScheduleEntity>> = dao.forDay(day)
    fun myBookings(uid: String): Flow<List<FmScheduleEntity>> = dao.myBookings(uid)

    /** Returns true if the slot was successfully booked (false if someone beat you to it). */
    suspend fun bookSlot(id: String, uid: String, name: String): Boolean = dao.bookSlot(id, uid, name) > 0

    suspend fun cancelBooking(id: String, uid: String): Boolean = dao.cancelBooking(id, uid) > 0

    suspend fun attachContent(id: String, uid: String, contentId: String, contentTitle: String) =
        dao.attachContent(id, uid, contentId, contentTitle)
}
