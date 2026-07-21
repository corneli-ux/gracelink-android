package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.ArticleDao
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.dao.ForumDao
import com.gracelink.android.data.db.dao.PodcastDao
import com.gracelink.android.data.db.entity.ArticleEntity
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import com.gracelink.android.data.db.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One item in the Timeline -- a social-media-style feed of everything a
 * followed church or individual pastor has published, merged and sorted
 * by recency across five different content types.
 */
sealed class TimelineItem(open val timestamp: Long, open val authorId: String, open val authorName: String) {
    data class Article(val entity: ArticleEntity) : TimelineItem(entity.publishedAt, entity.authorId, entity.authorName)
    data class Podcast(val entity: PodcastSeriesEntity) : TimelineItem(entity.createdAt, entity.authorId, entity.authorName)
    data class Prayer(val entity: PrayerRequest) : TimelineItem(entity.timestamp, entity.authorId, entity.authorName)
    data class Event(val entity: ChurchEventEntity) : TimelineItem(entity.startTime, entity.churchId, entity.churchName)
    data class Question(val entity: QuestionEntity) : TimelineItem(entity.createdAt, entity.authorId, entity.authorName)

    /** A stable (contentType, contentId) pair for reactions/comments, shared across every item type. */
    val contentType: String get() = when (this) {
        is Article -> "article"; is Podcast -> "podcast"; is Prayer -> "prayer"; is Event -> "event"; is Question -> "question"
    }
    val contentId: String get() = when (this) {
        is Article -> entity.id; is Podcast -> entity.id; is Prayer -> entity.id; is Event -> entity.id; is Question -> entity.id
    }
}

private data class TimelineBase(
    val articles: List<ArticleEntity>,
    val podcasts: List<PodcastSeriesEntity>,
    val prayers: List<PrayerRequest>,
    val events: List<ChurchEventEntity>,
)

@Singleton
class TimelineRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val podcastDao: PodcastDao,
    private val eventDao: ChurchEventDao,
    private val forumDao: ForumDao,
    private val prayerRepo: PrayerFirestoreRepository,
) {
    /** Feed built only from ids the caller actually follows -- an empty
     * follow list means an empty timeline, not everyone's content. */
    fun feedFor(followedIds: List<String>): Flow<List<TimelineItem>> {
        if (followedIds.isEmpty()) return flowOf(emptyList())
        val followedSet = followedIds.toSet()

        val baseFlow = combine(
            articleDao.all(), podcastDao.allSeries(), prayerRepo.allPrayers(), eventDao.all(),
        ) { articles, podcasts, prayers, events -> TimelineBase(articles, podcasts, prayers, events) }

        return combine(baseFlow, forumDao.allQuestions()) { base, questions ->
            (base.articles.filter { it.authorId in followedSet }.map { TimelineItem.Article(it) } +
                base.podcasts.filter { it.authorId in followedSet }.map { TimelineItem.Podcast(it) } +
                base.prayers.filter { it.authorId in followedSet }.map { TimelineItem.Prayer(it) } +
                base.events.filter { it.churchId in followedSet }.map { TimelineItem.Event(it) } +
                questions.filter { it.authorId in followedSet }.map { TimelineItem.Question(it) })
                .sortedByDescending { it.timestamp }
        }
    }
}
