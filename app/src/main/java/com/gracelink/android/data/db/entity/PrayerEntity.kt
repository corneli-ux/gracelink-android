package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PrayerStatus { PENDING, APPROVED, REJECTED }

@Entity(tableName = "prayers")
data class PrayerEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val displayName: String?,
    val text: String,
    val timestamp: Long,
    val prayedCount: Int,
    val isAnswered: Boolean,
    val isMine: Boolean,
    val userPrayedThis: Boolean,
    val status: PrayerStatus,
    // Encouragements stored as JSON array string for simplicity
    val encouragementsJson: String,
)
