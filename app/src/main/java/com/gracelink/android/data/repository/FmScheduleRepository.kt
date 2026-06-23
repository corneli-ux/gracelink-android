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
}
