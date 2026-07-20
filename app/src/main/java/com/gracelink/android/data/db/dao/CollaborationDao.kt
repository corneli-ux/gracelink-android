package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracelink.android.data.db.entity.CollaborationRequestEntity
import com.gracelink.android.data.db.entity.CollaborationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CollaborationDao {
    @Query("SELECT * FROM collaboration_requests WHERE toChurchId = :churchId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun pendingFor(churchId: String): Flow<List<CollaborationRequestEntity>>

    @Query("SELECT * FROM collaboration_requests WHERE fromUid = :uid ORDER BY createdAt DESC")
    fun sentBy(uid: String): Flow<List<CollaborationRequestEntity>>

    @Query("SELECT * FROM collaboration_requests WHERE fromUid = :uid AND toChurchId = :churchId LIMIT 1")
    suspend fun existingRequest(uid: String, churchId: String): CollaborationRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: CollaborationRequestEntity)

    @Query("UPDATE collaboration_requests SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: CollaborationStatus)
}
