package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LiveSessionStatus { UPCOMING, LIVE, ENDED }

@Entity(tableName = "live_sessions")
data class LiveSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val hostsJson: String,        // JSON array of host names
    val startTime: Long,
    val endTime: Long,
    val status: LiveSessionStatus,
    val participantCount: Int,
    val streamUrl: String?,
    val chatEnabled: Boolean,
    val language: ContentLanguage,
    val category: ContentCategory,
    val coverImageUrl: String?,
    val remindMe: Boolean = false,
    val joinedQueue: Boolean = false,
)
