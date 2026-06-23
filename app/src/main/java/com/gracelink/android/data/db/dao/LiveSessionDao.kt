package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.db.entity.LiveSessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveSessionDao {

    @Query("SELECT * FROM live_sessions ORDER BY startTime ASC")
    fun all(): Flow<List<LiveSessionEntity>>

    @Query("SELECT * FROM live_sessions WHERE status = :status")
    fun byStatus(status: LiveSessionStatus): Flow<List<LiveSessionEntity>>

    @Query("SELECT * FROM live_sessions WHERE id = :id")
    suspend fun getById(id: String): LiveSessionEntity?

    @Update
    suspend fun update(session: LiveSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LiveSessionEntity>)
}
