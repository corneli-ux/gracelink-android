package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    // Multi-user account fields
    val accountType: AccountType = AccountType.PERSONAL,
    val beliefSystem: BeliefSystem = BeliefSystem.NONDENOMINATIONAL,
    val churchId: String? = null,         // if personal account joined a church
    val isVerified: Boolean = false,      // church verification status
    val bio: String? = null,
)
