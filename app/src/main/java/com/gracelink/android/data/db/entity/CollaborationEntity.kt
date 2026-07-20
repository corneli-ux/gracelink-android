package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CollaborationStatus { PENDING, ACCEPTED, DECLINED }

/**
 * A church or individual pastor proposing to partner with ANOTHER church
 * on events, online debates, or discussions -- distinct from
 * ChurchMemberEntity, which is an individual joining a congregation as a
 * member. Churches/pastors don't "join" each other; they collaborate.
 */
@Entity(tableName = "collaboration_requests")
data class CollaborationRequestEntity(
    @PrimaryKey val id: String,
    val fromUid: String,
    val fromName: String,
    val fromType: AccountType,
    val toChurchId: String,
    val toChurchName: String,
    val message: String,
    val status: CollaborationStatus = CollaborationStatus.PENDING,
    val createdAt: Long,
)
