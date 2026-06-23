package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracelink.android.data.db.entity.FmScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FmScheduleDao {
    @Query("SELECT * FROM fm_schedule ORDER BY id ASC")
    fun all(): Flow<List<FmScheduleEntity>>

    @Query("SELECT * FROM fm_schedule WHERE day = :day ORDER BY startHour ASC")
    fun forDay(day: String): Flow<List<FmScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FmScheduleEntity>)

    @Query("DELETE FROM fm_schedule")
    suspend fun clear()
}
