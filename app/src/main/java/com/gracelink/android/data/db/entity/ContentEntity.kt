package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ContentType { LIVE_RADIO, SERMON, PODCAST, DEBATE, WORSHIP }
enum class ContentLanguage { EN, TE, HI, TA, ML, KN }
enum class ContentCategory { WORSHIP, TEACHING, DEBATES, REGIONAL, TESTIMONY, YOUTH }

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val speaker: String?,
    val durationMs: Long,
    val audioUrl: String,
    val type: ContentType,
    val language: ContentLanguage,
    val category: ContentCategory,
    val thumbnailUrl: String?,
    val isDownloadable: Boolean,
    val publishedAt: Long,
    val isLive: Boolean,
    val listenerCount: Int,
)
