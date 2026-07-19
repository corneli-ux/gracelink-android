package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.ContentDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.ContentType
import com.gracelink.android.data.db.entity.PodcastEpisodeEntity
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastRepository @Inject constructor(
    private val dao: PodcastDao,
    private val contentDao: ContentDao,
) {
    fun allSeries(): Flow<List<PodcastSeriesEntity>> = dao.allSeries()
    fun seriesByAuthor(authorId: String): Flow<List<PodcastSeriesEntity>> = dao.seriesByAuthor(authorId)
    suspend fun seriesById(id: String): PodcastSeriesEntity? = dao.seriesById(id)

    fun allEpisodes(): Flow<List<PodcastEpisodeEntity>> = dao.allEpisodes()
    fun episodesFor(podcastId: String): Flow<List<PodcastEpisodeEntity>> = dao.episodesFor(podcastId)
    suspend fun episodeById(id: String): PodcastEpisodeEntity? = dao.episodeById(id)

    suspend fun createSeries(
        authorId: String,
        authorName: String,
        authorType: AccountType,
        churchId: String?,
        title: String,
        description: String,
        coverUrl: String?,
        category: String,
    ): String {
        val id = "pod_${System.currentTimeMillis()}"
        dao.insertSeries(
            PodcastSeriesEntity(
                id = id,
                authorId = authorId,
                authorName = authorName,
                authorType = authorType,
                churchId = churchId,
                title = title,
                description = description,
                coverUrl = coverUrl,
                category = category,
                createdAt = System.currentTimeMillis(),
            )
        )
        return id
    }

    /**
     * Adds an episode AND mirrors it into the shared `content` table (same
     * id) so the existing universal Player screen -- which only knows how
     * to look up ContentEntity -- can actually play it. The podcast tables
     * remain the source of truth for series/episode metadata.
     */
    suspend fun addEpisode(podcastId: String, title: String, audioUrl: String, durationLabel: String) {
        val id = "ep_${System.currentTimeMillis()}"
        val series = dao.seriesById(podcastId)
        dao.insertEpisode(
            PodcastEpisodeEntity(
                id = id,
                podcastId = podcastId,
                title = title,
                audioUrl = audioUrl,
                durationLabel = durationLabel,
                publishedAt = System.currentTimeMillis(),
            )
        )
        contentDao.insertAll(
            listOf(
                ContentEntity(
                    id = id,
                    title = title,
                    description = series?.description ?: "",
                    speaker = series?.authorName,
                    durationMs = 0,
                    audioUrl = audioUrl,
                    type = ContentType.PODCAST,
                    language = ContentLanguage.EN,
                    category = ContentCategory.TEACHING,
                    thumbnailUrl = series?.coverUrl,
                    isDownloadable = true,
                    publishedAt = System.currentTimeMillis(),
                    isLive = false,
                    listenerCount = 0,
                )
            )
        )
    }
}
