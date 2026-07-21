package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.ArticleDao
import com.gracelink.android.data.db.dao.ChurchEventDao
import com.gracelink.android.data.db.entity.AnnouncementEntity
import com.gracelink.android.data.db.entity.ArticleEntity
import com.gracelink.android.data.db.entity.ChurchEventEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One item in a church's activity feed -- announcements, events, and
 * articles merged into a single, time-sorted stream so a member sees
 * everything their church has posted in one place, on Home, rather
 * than having to check three separate screens to notice anything new.
 */
sealed class ChurchActivityItem(open val timestamp: Long) {
    data class Announcement(val entity: AnnouncementEntity) : ChurchActivityItem(entity.createdAt)
    data class Event(val entity: ChurchEventEntity) : ChurchActivityItem(entity.startTime)
    data class Article(val entity: ArticleEntity) : ChurchActivityItem(entity.publishedAt)
}

@Singleton
class ChurchActivityRepository @Inject constructor(
    private val adminRepo: ChurchAdminRepository,
    private val eventDao: ChurchEventDao,
    private val articleDao: ArticleDao,
) {
    /** Feed for a single church, newest first. */
    fun feedFor(churchId: String): Flow<List<ChurchActivityItem>> = combine(
        adminRepo.announcements(churchId),
        eventDao.forChurch(churchId),
        articleDao.forChurch(churchId),
    ) { announcements, events, articles ->
        (announcements.map { ChurchActivityItem.Announcement(it) } +
            events.map { ChurchActivityItem.Event(it) } +
            articles.map { ChurchActivityItem.Article(it) })
            .sortedByDescending { it.timestamp }
    }

    /** Feed merged across every church id the caller belongs to (a person can only
     * be an approved member of one church today, but this stays correct if that changes). */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun feedForChurches(churchIds: List<String>): Flow<List<ChurchActivityItem>> {
        if (churchIds.isEmpty()) return flowOf(emptyList())
        val flows = churchIds.map { feedFor(it) }
        return combine(flows) { arrays -> arrays.flatMap { it }.sortedByDescending { it.timestamp } }
    }
}
