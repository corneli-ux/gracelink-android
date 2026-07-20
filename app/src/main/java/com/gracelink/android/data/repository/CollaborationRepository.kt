package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.CollaborationDao
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.CollaborationRequestEntity
import com.gracelink.android.data.db.entity.CollaborationStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollaborationRepository @Inject constructor(
    private val dao: CollaborationDao,
) {
    fun pendingFor(churchId: String): Flow<List<CollaborationRequestEntity>> = dao.pendingFor(churchId)
    fun sentBy(uid: String): Flow<List<CollaborationRequestEntity>> = dao.sentBy(uid)

    suspend fun requestCollaboration(
        fromUid: String,
        fromName: String,
        fromType: AccountType,
        toChurchId: String,
        toChurchName: String,
        message: String,
    ): Boolean {
        val existing = dao.existingRequest(fromUid, toChurchId)
        if (existing != null) return false
        dao.insert(
            CollaborationRequestEntity(
                id = "collab_${System.currentTimeMillis()}",
                fromUid = fromUid, fromName = fromName, fromType = fromType,
                toChurchId = toChurchId, toChurchName = toChurchName,
                message = message, status = CollaborationStatus.PENDING,
                createdAt = System.currentTimeMillis(),
            )
        )
        return true
    }

    suspend fun respond(id: String, accept: Boolean) {
        dao.setStatus(id, if (accept) CollaborationStatus.ACCEPTED else CollaborationStatus.DECLINED)
    }
}
