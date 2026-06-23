package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.DownloadDao
import com.gracelink.android.data.db.dao.UserDao
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.DownloadEntity
import com.gracelink.android.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
) {
    fun current(): Flow<UserEntity?> = userDao.current()
    suspend fun currentOnce(): UserEntity? = userDao.currentOnce()

    suspend fun setLanguage(uid: String, lang: ContentLanguage) = userDao.setLanguage(uid, lang)
    suspend fun setDataSaver(uid: String, enabled: Boolean) = userDao.setDataSaver(uid, enabled)
    suspend fun setNotifications(uid: String, enabled: Boolean) = userDao.setNotifications(uid, enabled)
}

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao,
) {
    fun all(): Flow<List<DownloadEntity>> = downloadDao.all()
    fun isDownloaded(id: String): Flow<Boolean> = downloadDao.isDownloaded(id)

    suspend fun add(contentId: String, title: String, audioUrl: String, sizeBytes: Long) {
        downloadDao.add(
            DownloadEntity(
                contentId = contentId,
                title = title,
                audioUrl = audioUrl,
                downloadedAt = System.currentTimeMillis(),
                sizeBytes = sizeBytes,
            )
        )
    }

    suspend fun remove(contentId: String) = downloadDao.removeById(contentId)
}
