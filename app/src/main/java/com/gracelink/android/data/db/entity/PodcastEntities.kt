package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcast_series")
data class PodcastSeriesEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,       // pastor or church display name
    val authorType: AccountType,
    val churchId: String?,
    val title: String,
    val description: String,
    val coverUrl: String?,
    val category: String,         // Teaching, Worship, Regional, etc.
    val createdAt: Long,
)

@Entity(tableName = "podcast_episodes")
data class PodcastEpisodeEntity(
    @PrimaryKey val id: String,
    val podcastId: String,
    val title: String,
    val audioUrl: String,
    val durationLabel: String,    // "28 min" -- display label, not parsed
    val publishedAt: Long,
)
