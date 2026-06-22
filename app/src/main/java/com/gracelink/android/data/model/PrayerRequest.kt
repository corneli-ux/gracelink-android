package com.gracelink.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A community prayer request. Spec §6 + §8 moderation rules —
 *  - All prayers start in PENDING state
 *  - Admin approves before they appear publicly
 *  - Anonymous option supported
 */
@Serializable
data class PrayerRequest(
    val id: String,
    val userId: String?,
    val displayName: String?,    // null for anonymous
    val text: String,
    val timestamp: Long,         // epoch millis
    val prayedCount: Int = 0,
    val encouragements: List<Encouragement> = emptyList(),
    val isAnswered: Boolean = false,
    val isMine: Boolean = false,
    val userPrayedThis: Boolean = false,
)

@Serializable
data class Encouragement(
    val id: String,
    val userId: String?,
    val displayName: String?,
    val text: String,
    val timestamp: Long,
)
