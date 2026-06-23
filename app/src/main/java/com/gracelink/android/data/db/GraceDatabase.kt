package com.gracelink.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gracelink.android.data.db.converter.EnumConverters
import com.gracelink.android.data.db.dao.ChatDao
import com.gracelink.android.data.db.dao.ContentDao
import com.gracelink.android.data.db.dao.DownloadDao
import com.gracelink.android.data.db.dao.FavoriteDao
import com.gracelink.android.data.db.dao.FmScheduleDao
import com.gracelink.android.data.db.dao.HistoryDao
import com.gracelink.android.data.db.dao.LiveSessionDao
import com.gracelink.android.data.db.dao.PrayerDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ChatMessageEntity
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.DownloadEntity
import com.gracelink.android.data.db.entity.FavoriteEntity
import com.gracelink.android.data.db.entity.FmScheduleEntity
import com.gracelink.android.data.db.entity.HistoryEntity
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.db.entity.PrayerEntity
import com.gracelink.android.data.db.entity.UserEntity

@Database(
    entities = [
        ContentEntity::class,
        LiveSessionEntity::class,
        PrayerEntity::class,
        ChatMessageEntity::class,
        UserEntity::class,
        FavoriteEntity::class,
        DownloadEntity::class,
        HistoryEntity::class,
        FmScheduleEntity::class,
    ],
    version = 3,
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
}
