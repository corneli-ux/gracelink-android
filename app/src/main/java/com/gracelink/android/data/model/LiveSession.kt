package com.gracelink.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LiveSessionStatus {
    @SerialName("upcoming") UPCOMING,
    @SerialName("live")     LIVE,
    @SerialName("ended")    ENDED,
}

/**
 * A scheduled live event — debate, Q&A, call-in show, or live worship night.
 * Per spec §6.
 */
@Serializable
data class LiveSession(
    val id: String,
    val title: String,
    val description: String,
    val hosts: List<String> = emptyList(),
    val startTime: Long,         // epoch millis
    val endTime: Long,
    val status: LiveSessionStatus,
    val participantCount: Int = 0,
    val streamUrl: String? = null,
    val chatEnabled: Boolean = true,
    val language: ContentLanguage = ContentLanguage.EN,
    val category: ContentCategory = ContentCategory.DEBATES,
    val coverImageUrl: String? = null,
    val remindMe: Boolean = false,
    val joinedQueue: Boolean = false,
)
