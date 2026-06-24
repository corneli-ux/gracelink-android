package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gracelink.android.data.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChurchDao {
    @Query("SELECT * FROM churches ORDER BY memberCount DESC")
    fun all(): Flow<List<ChurchEntity>>

    @Query("SELECT * FROM churches WHERE id = :id")
    suspend fun getById(id: String): ChurchEntity?

    @Query("SELECT * FROM churches WHERE verificationStatus = 'VERIFIED'")
    fun verified(): Flow<List<ChurchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(church: ChurchEntity)

    @Update
    suspend fun update(church: ChurchEntity)
}

@Dao
interface ChurchMemberDao {
    @Query("SELECT * FROM church_members WHERE churchId = :churchId ORDER BY joinedAt DESC")
    fun forChurch(churchId: String): Flow<List<ChurchMemberEntity>>

    @Query("SELECT * FROM church_members WHERE churchId = :churchId AND status = 'APPROVED' ORDER BY joinedAt DESC")
    fun approvedForChurch(churchId: String): Flow<List<ChurchMemberEntity>>

    @Query("SELECT * FROM church_members WHERE churchId = :churchId AND status = 'PENDING' ORDER BY joinedAt DESC")
    fun pendingForChurch(churchId: String): Flow<List<ChurchMemberEntity>>

    @Query("SELECT * FROM church_members WHERE userId = :userId")
    fun forUser(userId: String): Flow<List<ChurchMemberEntity>>

    @Query("SELECT COUNT(*) FROM church_members WHERE churchId = :churchId AND status = 'APPROVED'")
    suspend fun countApproved(churchId: String): Int

    @Query("SELECT COUNT(*) FROM church_members WHERE churchId = :churchId AND status = 'PENDING'")
    suspend fun countPending(churchId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: ChurchMemberEntity)

    @Query("UPDATE church_members SET status = 'APPROVED', approvedAt = :now, isActive = 1 WHERE id = :memberId")
    suspend fun approve(memberId: String, now: Long)

    @Query("UPDATE church_members SET status = 'REJECTED', isActive = 0 WHERE id = :memberId")
    suspend fun reject(memberId: String)

    @Query("UPDATE church_members SET isActive = 0 WHERE churchId = :churchId AND userId = :userId")
    suspend fun deactivate(churchId: String, userId: String)
}

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun all(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE churchId = :churchId ORDER BY publishedAt DESC")
    fun forChurch(churchId: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE authorId = :authorId ORDER BY publishedAt DESC")
    fun forAuthor(authorId: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: String): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: ArticleEntity)

    @Query("UPDATE articles SET likeCount = likeCount + 1 WHERE id = :id")
    suspend fun incrementLikes(id: String)

    @Query("UPDATE articles SET likeCount = likeCount - 1 WHERE id = :id AND likeCount > 0")
    suspend fun decrementLikes(id: String)

    @Query("UPDATE articles SET commentCount = commentCount + 1 WHERE id = :id")
    suspend fun incrementComments(id: String)
}

@Dao
interface ArticleCommentDao {
    @Query("SELECT * FROM article_comments WHERE articleId = :articleId ORDER BY timestamp ASC")
    fun forArticle(articleId: String): Flow<List<ArticleCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: ArticleCommentEntity)
}

@Dao
interface ArticleLikeDao {
    @Query("SELECT EXISTS(SELECT 1 FROM article_likes WHERE articleId = :articleId AND userId = :userId)")
    fun isLiked(articleId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(like: ArticleLikeEntity)

    @Query("DELETE FROM article_likes WHERE articleId = :articleId AND userId = :userId")
    suspend fun remove(articleId: String, userId: String)
}

@Dao
interface ChurchEventDao {
    @Query("SELECT * FROM church_events ORDER BY startTime ASC")
    fun all(): Flow<List<ChurchEventEntity>>

    @Query("SELECT * FROM church_events WHERE churchId = :churchId ORDER BY startTime ASC")
    fun forChurch(churchId: String): Flow<List<ChurchEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ChurchEventEntity)
}

@Dao
interface FaithProgressDao {
    @Query("SELECT * FROM faith_progress WHERE userId = :userId")
    fun forUser(userId: String): Flow<FaithProgressEntity?>

    @Query("SELECT * FROM faith_progress WHERE userId = :userId")
    suspend fun getOnce(userId: String): FaithProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: FaithProgressEntity)

    @Query("UPDATE faith_progress SET bibleReadingDays = bibleReadingDays + 1, lastProgressAt = :now WHERE userId = :userId")
    suspend fun incrementBibleReading(userId: String, now: Long)

    @Query("UPDATE faith_progress SET prayerSessions = prayerSessions + 1, lastProgressAt = :now WHERE userId = :userId")
    suspend fun incrementPrayer(userId: String, now: Long)
}
