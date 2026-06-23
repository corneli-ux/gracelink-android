package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { PERSONAL, CHURCH }

enum class BeliefSystem(val displayName: String) {
    PROGRESSIVE_SANCTIFICATION("Progressive Sanctification"),
    REFORMED("Reformed / Calvinist"),
    ARMINIAN("Arminian"),
    WESLEYAN("Wesleyan / Holiness"),
    DISPENSATIONAL("Dispensational"),
    COVENANT("Covenant Theology"),
    PENTECOSTAL("Pentecostal / Charismatic"),
    BAPTIST("Baptist"),
    LUTHERAN("Lutheran"),
    ANGLICAN("Anglican"),
    ORTHODOX("Eastern Orthodox"),
    NONDENOMINATIONAL("Non-Denominational"),
}

enum class VerificationStatus { UNVERIFIED, PENDING, VERIFIED, SUSPENDED }

@Entity(tableName = "churches")
data class ChurchEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val pastorName: String,
    val location: String,
    val beliefSystem: BeliefSystem,
    val verificationStatus: VerificationStatus,
    val certificateUrl: String?,      // Church registration certificate
    val photoUrl: String?,            // Church building photo
    val memberCount: Int,
    val createdAt: Long,
    val gracePeriodEndsAt: Long,      // Suspension deadline if not progressing
    val website: String?,
    val phone: String?,
)

@Entity(tableName = "church_members")
data class ChurchMemberEntity(
    @PrimaryKey val id: String,       // "${churchId}_${userId}"
    val churchId: String,
    val userId: String,
    val displayName: String,
    val joinedAt: Long,
    val beliefSystem: BeliefSystem,
    val isActive: Boolean,
)

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorType: AccountType,      // PERSONAL or CHURCH
    val churchId: String?,            // null if personal account
    val title: String,
    val content: String,
    val publishedAt: Long,
    val likeCount: Int,
    val commentCount: Int,
    val tags: String,                 // comma-separated
)

@Entity(tableName = "article_comments")
data class ArticleCommentEntity(
    @PrimaryKey val id: String,
    val articleId: String,
    val userId: String,
    val displayName: String,
    val text: String,
    val timestamp: Long,
)

@Entity(tableName = "article_likes")
data class ArticleLikeEntity(
    @PrimaryKey val id: String,       // "${articleId}_${userId}"
    val articleId: String,
    val userId: String,
    val timestamp: Long,
)

@Entity(tableName = "church_events")
data class ChurchEventEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val churchName: String,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val isOnline: Boolean,
    val meetingLink: String?,
    val location: String?,
    val category: String,             // WORSHIP, TEACHING, YOUTH, PRAYER, FELLOWSHIP
    val attendeeCount: Int,
)

@Entity(tableName = "faith_progress")
data class FaithProgressEntity(
    @PrimaryKey val userId: String,
    val beliefSystem: BeliefSystem,
    val bibleReadingDays: Int,        // days read Bible this week
    val prayerSessions: Int,          // prayer sessions this week
    val churchAttendances: Int,       // services attended this month
    val articlesWritten: Int,
    val prayersOffered: Int,
    val membersDiscipled: Int,
    val lastProgressAt: Long,
    val sanctificationLevel: Int,     // 0-100 progress score
    val gracePeriodEndsAt: Long,      // suspension deadline
)
