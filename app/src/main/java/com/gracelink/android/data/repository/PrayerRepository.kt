package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.ChatDao
import com.gracelink.android.data.db.dao.PrayerDao
import com.gracelink.android.data.db.entity.ChatMessageEntity
import com.gracelink.android.data.db.entity.PrayerEntity
import com.gracelink.android.data.db.entity.PrayerStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerRepository @Inject constructor(
    private val prayerDao: PrayerDao,
    private val chatDao: ChatDao,
) {
    fun approved(): Flow<List<PrayerEntity>> = prayerDao.approved()
    fun mine(): Flow<List<PrayerEntity>> = prayerDao.mine()
    fun answered(): Flow<List<PrayerEntity>> = prayerDao.answered()
    fun pending(): Flow<List<PrayerEntity>> = prayerDao.pending()

    /**
     * Submit a new prayer. Per spec §8, all prayers start in PENDING state
     * and require admin approval before appearing publicly. For the MVP,
     * we auto-approve so the user can see their submission — flip the
     * status to PENDING when wiring the admin dashboard.
     */
    suspend fun submit(text: String, anonymous: Boolean, myName: String) {
        val prayer = PrayerEntity(
            id = "pr_${System.currentTimeMillis()}",
            userId = if (anonymous) null else "u_demo",
            displayName = if (anonymous) null else myName,
            text = text,
            timestamp = System.currentTimeMillis(),
            prayedCount = 0,
            isAnswered = false,
            isMine = true,
            userPrayedThis = false,
            status = PrayerStatus.APPROVED, // auto-approve for MVP demo
            encouragementsJson = "[]",
        )
        prayerDao.insert(prayer)
    }

    suspend fun togglePrayed(id: String) {
        val p = prayerDao.getById(id) ?: return
        val nowPrayed = !p.userPrayedThis
        prayerDao.update(
            p.copy(
                userPrayedThis = nowPrayed,
                prayedCount = if (nowPrayed) p.prayedCount + 1 else (p.prayedCount - 1).coerceAtLeast(0)
            )
        )
    }

    suspend fun markAnswered(id: String) {
        val p = prayerDao.getById(id) ?: return
        prayerDao.update(p.copy(isAnswered = true))
    }

    suspend fun addEncouragement(prayerId: String, text: String, myName: String) {
        val p = prayerDao.getById(prayerId) ?: return
        val encJson = p.encouragementsJson.trimEnd(']')
        val newEntry = """{"id":"e_${System.currentTimeMillis()}","userId":"u_demo","displayName":"$myName","text":"${text.replace("\"", "\\\"")}","timestamp":${System.currentTimeMillis()}}"""
        val updated = if (encJson == "[") "$encJson$newEntry]" else "$encJson,$newEntry]"
        prayerDao.update(p.copy(encouragementsJson = updated))
    }

    fun chatFor(sessionId: String): Flow<List<ChatMessageEntity>> = chatDao.forSession(sessionId)

    suspend fun sendMessage(sessionId: String, displayName: String, text: String, isQuestion: Boolean) {
        chatDao.insert(
            ChatMessageEntity(
                id = "m_${System.currentTimeMillis()}",
                sessionId = sessionId,
                userId = "u_demo",
                displayName = displayName,
                text = text,
                timestamp = System.currentTimeMillis(),
                isModerator = false,
                isHost = false,
                isQuestion = isQuestion,
                isMine = true,
            )
        )
    }
}
