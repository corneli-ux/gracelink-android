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

    // -- Live slot booking --------------------------------------------------
    @Query("UPDATE fm_schedule SET bookedByUid = :uid, bookedByName = :name, preacher = :name WHERE id = :id AND bookedByUid IS NULL")
    suspend fun bookSlot(id: String, uid: String, name: String): Int

    @Query("UPDATE fm_schedule SET bookedByUid = NULL, bookedByName = NULL WHERE id = :id AND bookedByUid = :uid")
    suspend fun cancelBooking(id: String, uid: String): Int

    @Query("SELECT * FROM fm_schedule WHERE bookedByUid = :uid ORDER BY id ASC")
    fun myBookings(uid: String): Flow<List<FmScheduleEntity>>

    @Query("UPDATE fm_schedule SET contentId = :contentId, contentTitle = :contentTitle WHERE id = :id AND bookedByUid = :uid")
    suspend fun attachContent(id: String, uid: String, contentId: String, contentTitle: String)
}
