package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { PERSONAL, PASTOR, CHURCH }

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

enum class MemberStatus { PENDING, APPROVED, REJECTED, INACTIVE, REMOVED }

/** Roles a person can hold inside a specific church. */
enum class ChurchRole {
    MEMBER,
    LEADER,          // small group / ministry leader
    ELDER,
    DEACON,
    PASTOR,
    ADMIN,           // full church admin rights
    OWNER            // the account that created the church record
}

enum class GroupType {
    SMALL_GROUP,
    MINISTRY,
    PRAYER_CHAIN,
    YOUTH,
    WOMEN,
    MEN,
    WORSHIP,
    CUSTOM
}

enum class AnnouncementPriority { NORMAL, HIGH, URGENT }

enum class RsvpStatus { GOING, MAYBE, NOT_GOING }

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
    val ownerUserId: String? = null,  // links a CHURCH-role profile to this record
    // New public fields
    val vision: String? = null,
    val serviceTimesJson: String? = null, // JSON array of service times
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
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
    val status: MemberStatus = MemberStatus.PENDING,
    val approvedAt: Long? = null,
    // New admin fields
    val role: ChurchRole = ChurchRole.MEMBER,
    val adminNotes: String? = null,   // private notes visible only to admins
    val phone: String? = null,
    val email: String? = null,
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
    val isPost: Boolean = false,      // true = short-form post, false = long-form article
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
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null, // simple RRULE or null
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

// ─────────────────────────────────────────────────────────────
// New entities for full church administration
// ─────────────────────────────────────────────────────────────

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val authorId: String,
    val authorName: String,
    val title: String,
    val body: String,
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val createdAt: Long,
    val expiresAt: Long? = null,
    val targetRolesJson: String? = null, // JSON list of ChurchRole names, null = all members
    val isPinned: Boolean = false,
)

@Entity(tableName = "church_groups")
data class ChurchGroupEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val name: String,
    val description: String,
    val type: GroupType = GroupType.CUSTOM,
    val leaderUserId: String? = null,
    val leaderName: String? = null,
    val memberCount: Int = 0,
    val createdAt: Long,
    val isPrivate: Boolean = false,   // only visible to members of the group
    val coverImageUrl: String? = null,
)

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey val id: String,       // "${groupId}_${userId}"
    val groupId: String,
    val churchId: String,
    val userId: String,
    val displayName: String,
    val role: ChurchRole = ChurchRole.MEMBER, // role inside this group
    val joinedAt: Long,
    val isActive: Boolean = true,
)

@Entity(tableName = "event_rsvps")
data class EventRsvpEntity(
    @PrimaryKey val id: String,       // "${eventId}_${userId}"
    val eventId: String,
    val churchId: String,
    val userId: String,
    val displayName: String,
    val status: RsvpStatus,
    val respondedAt: Long,
    val note: String? = null,
)

@Entity(tableName = "leadership_team")
data class LeadershipMemberEntity(
    @PrimaryKey val id: String,       // "${churchId}_${userId}"
    val churchId: String,
    val userId: String,
    val displayName: String,
    val title: String,                // e.g. "Senior Pastor", "Youth Pastor", "Elder"
    val bio: String? = null,
    val photoUrl: String? = null,
    val sortOrder: Int = 0,
    val isPublic: Boolean = true,
)

@Entity(tableName = "ministries")
data class MinistryEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val name: String,
    val description: String,
    val leaderUserId: String? = null,
    val leaderName: String? = null,
    val meetingInfo: String? = null,  // e.g. "Sundays 9am, Room 3"
    val coverImageUrl: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)

// ─────────────────────────────────────────────────────────────
// Messaging, service times, admin notes, moderation
// ─────────────────────────────────────────────────────────────

enum class MessageType { TEXT, IMAGE, AUDIO, SYSTEM }

@Entity(tableName = "group_messages")
data class GroupMessageEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val churchId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val createdAt: Long,
    val isDeleted: Boolean = false,
)

@Entity(tableName = "direct_messages")
data class DirectMessageEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val conversationId: String,            // sorted "uid1_uid2"
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val text: String,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val createdAt: Long,
    val isRead: Boolean = false,
)

@Entity(tableName = "service_times")
data class ServiceTimeEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val dayOfWeek: Int,                    // 1=Sunday ... 7=Saturday
    val time: String,                      // "09:00"
    val name: String,                      // "Sunday Worship", "Midweek Prayer"
    val location: String? = null,
    val isOnline: Boolean = false,
    val sortOrder: Int = 0,
)

@Entity(tableName = "admin_notes")
data class AdminNoteEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val memberUserId: String,
    val authorId: String,
    val authorName: String,
    val note: String,
    val createdAt: Long,
    val isPrivate: Boolean = true,
)

@Entity(tableName = "moderation_actions")
data class ModerationActionEntity(
    @PrimaryKey val id: String,
    val churchId: String,
    val actorId: String,
    val actorName: String,
    val targetUserId: String? = null,
    val targetContentId: String? = null,
    val action: String,                    // WARN, MUTE, REMOVE_CONTENT, SUSPEND, BAN
    val reason: String,
    val createdAt: Long,
)
