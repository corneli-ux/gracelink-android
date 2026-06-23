package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.ContentDao
import com.gracelink.android.data.db.dao.FavoriteDao
import com.gracelink.android.data.db.dao.HistoryDao
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.ContentType
import com.gracelink.android.data.db.entity.FavoriteEntity
import com.gracelink.android.data.db.entity.HistoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val contentDao: ContentDao,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
    private val firestore: FirebaseFirestore,
) {

    fun liveRadio(): Flow<List<ContentEntity>> = contentDao.liveRadio()
    fun library(): Flow<List<ContentEntity>> = contentDao.library()

    fun search(
        query: String,
        category: ContentCategory?,
        language: ContentLanguage?,
        type: ContentType?,
    ): Flow<List<ContentEntity>> = contentDao.search(query, category, language, type)

    suspend fun getById(id: String): ContentEntity? = contentDao.getById(id)

    fun favorites(): Flow<List<String>> = favoriteDao.all().map { list -> list.map { it.contentId } }
    fun isFavorite(id: String): Flow<Boolean> = favoriteDao.isFavorite(id)

    suspend fun toggleFavorite(id: String) {
        val exists = favoriteDao.isFavorite(id).first()
        if (exists) favoriteDao.removeById(id) else favoriteDao.add(FavoriteEntity(id, System.currentTimeMillis()))
    }

    suspend fun addFavorite(id: String) = favoriteDao.add(FavoriteEntity(id, System.currentTimeMillis()))
    suspend fun removeFavorite(id: String) = favoriteDao.removeById(id)

    fun history(): Flow<List<HistoryEntity>> = historyDao.recent()

    suspend fun addToHistory(content: ContentEntity, positionMs: Long) {
        historyDao.upsert(
            HistoryEntity(
                contentId = content.id,
                title = content.title,
                lastPlayedAt = System.currentTimeMillis(),
                positionMs = positionMs,
                durationMs = content.durationMs,
            )
        )
    }

    suspend fun updatePosition(contentId: String, positionMs: Long) =
        historyDao.updatePosition(contentId, positionMs)

    /**
     * Fetch all content from Firestore and cache in Room.
     * Called on app startup to sync with the cloud.
     */
    suspend fun syncFromFirestore() {
        try {
            val snapshot = firestore.collection("content").get().await()
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    ContentEntity(
                        id = data["id"] as? String ?: doc.id,
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        speaker = data["speaker"] as? String,
                        durationMs = (data["durationMs"] as? Number)?.toLong() ?: 0L,
                        audioUrl = data["audioUrl"] as? String ?: "",
                        type = ContentType.valueOf(data["type"] as? String ?: "PODCAST"),
                        language = ContentLanguage.valueOf(data["language"] as? String ?: "EN"),
                        category = ContentCategory.valueOf(data["category"] as? String ?: "TEACHING"),
                        thumbnailUrl = data["thumbnailUrl"] as? String,
                        isDownloadable = data["isDownloadable"] as? Boolean ?: true,
                        publishedAt = (data["publishedAt"] as? Number)?.toLong() ?: 0L,
                        isLive = data["isLive"] as? Boolean ?: false,
                        listenerCount = (data["listenerCount"] as? Number)?.toInt() ?: 0,
                    )
                } catch (_: Exception) { null }
            }
            contentDao.insertAll(items)
        } catch (_: Exception) {
            // Firestore fetch failed — fall back to Room cache
        }
    }
}
