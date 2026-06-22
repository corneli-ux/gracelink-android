package com.gracelink.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Type discriminator for [ContentItem] — drives UI iconography and routing.
 */
@Serializable
enum class ContentType {
    @SerialName("live_radio") LIVE_RADIO,
    @SerialName("sermon")     SERMON,
    @SerialName("podcast")    PODCAST,
    @SerialName("debate")     DEBATE,
}

@Serializable
enum class ContentLanguage {
    @SerialName("en") EN,
    @SerialName("te") TE,
}

@Serializable
enum class ContentCategory(val displayKey: String) {
    @SerialName("worship")   WORSHIP("cat_worship"),
    @SerialName("teaching")  TEACHING("cat_teaching"),
    @SerialName("debates")   DEBATES("cat_debates"),
    @SerialName("regional")  REGIONAL("cat_regional"),
    @SerialName("testimony") TESTIMONY("cat_testimony"),
    @SerialName("youth")     YOUTH("cat_youth"),
}

/**
 * Unified content model — covers live radio channels, on-demand sermons,
 * podcasts, and debates. Per spec §6 "Core Data Models".
 */
@Serializable
data class ContentItem(
    val id: String,
    val title: String,
    val description: String,
    val speaker: String? = null,
    val durationMs: Long = 0L,         // 0 for live radio
    val audioUrl: String,
    val type: ContentType,
    val language: ContentLanguage = ContentLanguage.EN,
    val category: ContentCategory = ContentCategory.TEACHING,
    val thumbnailUrl: String? = null,
    val isDownloadable: Boolean = true,
    val publishedAt: Long = 0L,         // epoch millis
    val isLive: Boolean = false,
    val listenerCount: Int = 0,
) {
    val isLiveRadio get() = type == ContentType.LIVE_RADIO
}
