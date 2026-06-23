package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracelink.android.data.db.entity.ChatMessageEntity
import com.gracelink.android.data.db.entity.DownloadEntity
import com.gracelink.android.data.db.entity.FavoriteEntity
import com.gracelink.android.data.db.entity.HistoryEntity
import com.gracelink.android.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun forSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun current(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun currentOnce(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("UPDATE users SET preferredLanguage = :lang WHERE uid = :uid")
    suspend fun setLanguage(uid: String, lang: com.gracelink.android.data.db.entity.ContentLanguage)

    @Query("UPDATE users SET dataSaverEnabled = :enabled WHERE uid = :uid")
    suspend fun setDataSaver(uid: String, enabled: Boolean)

    @Query("UPDATE users SET notificationsEnabled = :enabled WHERE uid = :uid")
    suspend fun setNotifications(uid: String, enabled: Boolean)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun all(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(fav: FavoriteEntity)

    @Delete
    suspend fun remove(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE contentId = :id")
    suspend fun removeById(id: String)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun all(): Flow<List<DownloadEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE contentId = :id)")
    fun isDownloaded(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE contentId = :id")
    suspend fun removeById(id: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM listening_history ORDER BY lastPlayedAt DESC LIMIT 10")
    fun recent(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: HistoryEntity)

    @Query("UPDATE listening_history SET positionMs = :pos WHERE contentId = :id")
    suspend fun updatePosition(id: String, pos: Long)
}
