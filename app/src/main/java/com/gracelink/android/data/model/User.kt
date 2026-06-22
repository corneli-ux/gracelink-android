package com.gracelink.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val preferredLanguage: ContentLanguage = ContentLanguage.EN,
    val createdAt: Long = 0L,
    val listeningStats: ListeningStats = ListeningStats(),
    val dataSaverEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
)

@Serializable
data class ListeningStats(
    val totalMinutes: Int = 0,
    val completedItems: Int = 0,
    val prayersOffered: Int = 0,
    val streakDays: Int = 0,
)
