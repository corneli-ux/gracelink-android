package com.gracelink.android.di

import android.content.Context
import com.gracelink.android.data.db.DatabaseProvider
import com.gracelink.android.data.db.GraceDatabase
import com.gracelink.android.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): GraceDatabase = DatabaseProvider.get(ctx)

    @Provides fun contentDao(db: GraceDatabase): ContentDao = db.contentDao()
    @Provides fun liveSessionDao(db: GraceDatabase): LiveSessionDao = db.liveSessionDao()
    @Provides fun prayerDao(db: GraceDatabase): PrayerDao = db.prayerDao()
    @Provides fun chatDao(db: GraceDatabase): ChatDao = db.chatDao()
    @Provides fun userDao(db: GraceDatabase): UserDao = db.userDao()
    @Provides fun favoriteDao(db: GraceDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun downloadDao(db: GraceDatabase): DownloadDao = db.downloadDao()
    @Provides fun historyDao(db: GraceDatabase): HistoryDao = db.historyDao()
    @Provides fun fmScheduleDao(db: GraceDatabase): FmScheduleDao = db.fmScheduleDao()
    @Provides fun churchDao(db: GraceDatabase): ChurchDao = db.churchDao()
    @Provides fun churchMemberDao(db: GraceDatabase): ChurchMemberDao = db.churchMemberDao()
    @Provides fun articleDao(db: GraceDatabase): ArticleDao = db.articleDao()
    @Provides fun articleCommentDao(db: GraceDatabase): ArticleCommentDao = db.articleCommentDao()
    @Provides fun articleLikeDao(db: GraceDatabase): ArticleLikeDao = db.articleLikeDao()
    @Provides fun churchEventDao(db: GraceDatabase): ChurchEventDao = db.churchEventDao()
    @Provides fun faithProgressDao(db: GraceDatabase): FaithProgressDao = db.faithProgressDao()
    @Provides fun podcastDao(db: GraceDatabase): PodcastDao = db.podcastDao()
    @Provides fun forumDao(db: GraceDatabase): ForumDao = db.forumDao()
    @Provides fun collaborationDao(db: GraceDatabase): CollaborationDao = db.collaborationDao()
}
