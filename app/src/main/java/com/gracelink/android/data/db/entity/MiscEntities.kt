package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val userId: String?,
    val displayName: String,
    val text: String,
    val timestamp: Long,
    val isModerator: Boolean,
    val isHost: Boolean,
    val isQuestion: Boolean,
    val isMine: Boolean,
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?,
    val preferredLanguage: ContentLanguage,
    val createdAt: Long,
    val totalMinutes: Int,
    val completedItems: Int,
    val prayersOffered: Int,
    val streakDays: Int,
    val dataSaverEnabled: Boolean,
    val notificationsEnabled: Boolean,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val contentId: String,
    val addedAt: Long,
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val contentId: String,
    val title: String,
    val audioUrl: String,
    val downloadedAt: Long,
    val sizeBytes: Long,
)

@Entity(tableName = "listening_history")
data class HistoryEntity(
    @PrimaryKey val contentId: String,
    val title: String,
    val lastPlayedAt: Long,
    val positionMs: Long,
    val durationMs: Long,
)
