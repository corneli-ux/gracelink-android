package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.ContentType
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {

    @Query("SELECT * FROM content WHERE type = 'LIVE_RADIO'")
    fun liveRadio(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE type != 'LIVE_RADIO'")
    fun library(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE id = :id")
    suspend fun getById(id: String): ContentEntity?

    @Query("""
        SELECT * FROM content
        WHERE type != 'LIVE_RADIO'
        AND (:query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR speaker LIKE '%' || :query || '%')
        AND (:category IS NULL OR category = :category)
        AND (:language IS NULL OR language = :language)
        AND (:type IS NULL OR type = :type)
        ORDER BY publishedAt DESC
    """)
    fun search(query: String, category: ContentCategory?, language: ContentLanguage?, type: ContentType?): Flow<List<ContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ContentEntity>)
}
