package com.gracelink.android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gracelink.android.data.db.converter.EnumConverters
import com.gracelink.android.data.db.dao.*
import com.gracelink.android.data.db.entity.*

@Database(
    entities = [
        ContentEntity::class, LiveSessionEntity::class, PrayerEntity::class,
        ChatMessageEntity::class, UserEntity::class, FavoriteEntity::class,
        DownloadEntity::class, HistoryEntity::class, FmScheduleEntity::class,
        ChurchEntity::class, ChurchMemberEntity::class, ArticleEntity::class,
        ArticleCommentEntity::class, ArticleLikeEntity::class,
        ChurchEventEntity::class, FaithProgressEntity::class,
        PodcastSeriesEntity::class, PodcastEpisodeEntity::class,
        QuestionEntity::class, AnswerEntity::class,
        CollaborationRequestEntity::class,
        AnnouncementEntity::class, ChurchGroupEntity::class, GroupMemberEntity::class,
        EventRsvpEntity::class, LeadershipMemberEntity::class, MinistryEntity::class,
        GroupMessageEntity::class, DirectMessageEntity::class, ServiceTimeEntity::class,
        AdminNoteEntity::class, ModerationActionEntity::class,
    ],
    version = 14,
    exportSchema = false,
)
@TypeConverters(EnumConverters::class)
abstract class GraceDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun liveSessionDao(): LiveSessionDao
    abstract fun prayerDao(): PrayerDao
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun historyDao(): HistoryDao
    abstract fun fmScheduleDao(): FmScheduleDao
    abstract fun churchDao(): ChurchDao
    abstract fun churchMemberDao(): ChurchMemberDao
    abstract fun articleDao(): ArticleDao
    abstract fun articleCommentDao(): ArticleCommentDao
    abstract fun articleLikeDao(): ArticleLikeDao
    abstract fun churchEventDao(): ChurchEventDao
    abstract fun faithProgressDao(): FaithProgressDao
    abstract fun podcastDao(): PodcastDao
    abstract fun forumDao(): ForumDao
    abstract fun collaborationDao(): CollaborationDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun churchGroupDao(): ChurchGroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun eventRsvpDao(): EventRsvpDao
    abstract fun leadershipDao(): LeadershipDao
    abstract fun ministryDao(): MinistryDao
    abstract fun groupMessageDao(): GroupMessageDao
    abstract fun directMessageDao(): DirectMessageDao
    abstract fun serviceTimeDao(): ServiceTimeDao
    abstract fun adminNoteDao(): AdminNoteDao
    abstract fun moderationDao(): ModerationDao

    companion object {
        @Volatile private var INSTANCE: GraceDatabase? = null
        fun getInstance(context: Context): GraceDatabase =
            INSTANCE ?: synchronized(this) { INSTANCE ?: DatabaseProvider.get(context).also { INSTANCE = it } }
    }
}
