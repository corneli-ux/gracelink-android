package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracelink.android.data.db.entity.PodcastEpisodeEntity
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcast_series ORDER BY createdAt DESC")
    fun allSeries(): Flow<List<PodcastSeriesEntity>>

    @Query("SELECT * FROM podcast_series WHERE id = :id")
    suspend fun seriesById(id: String): PodcastSeriesEntity?

    @Query("SELECT * FROM podcast_series WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun seriesByAuthor(authorId: String): Flow<List<PodcastSeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: PodcastSeriesEntity)

    @Query("SELECT * FROM podcast_episodes ORDER BY publishedAt DESC")
    fun allEpisodes(): Flow<List<PodcastEpisodeEntity>>

    @Query("SELECT * FROM podcast_episodes WHERE podcastId = :podcastId ORDER BY publishedAt DESC")
    fun episodesFor(podcastId: String): Flow<List<PodcastEpisodeEntity>>

    @Query("SELECT * FROM podcast_episodes WHERE id = :id")
    suspend fun episodeById(id: String): PodcastEpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: PodcastEpisodeEntity)

    @Query("SELECT COUNT(*) FROM podcast_episodes WHERE podcastId = :podcastId")
    suspend fun episodeCount(podcastId: String): Int
}
