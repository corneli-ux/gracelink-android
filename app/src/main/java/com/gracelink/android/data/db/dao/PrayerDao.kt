package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gracelink.android.data.db.entity.PrayerEntity
import com.gracelink.android.data.db.entity.PrayerStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {

    @Query("SELECT * FROM prayers WHERE status = 'APPROVED' ORDER BY timestamp DESC")
    fun approved(): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayers WHERE isMine = 1 ORDER BY timestamp DESC")
    fun mine(): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayers WHERE isAnswered = 1 AND status = 'APPROVED' ORDER BY timestamp DESC")
    fun answered(): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayers WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun pending(): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayers WHERE id = :id")
    suspend fun getById(id: String): PrayerEntity?

    @Update
    suspend fun update(prayer: PrayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prayer: PrayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PrayerEntity>)
}
