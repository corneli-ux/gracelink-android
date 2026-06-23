package com.gracelink.android.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gracelink.android.data.db.entity.ChatMessageEntity
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.ContentType
import com.gracelink.android.data.db.entity.FmScheduleEntity
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.db.entity.LiveSessionStatus
import com.gracelink.android.data.db.entity.PrayerEntity
import com.gracelink.android.data.db.entity.PrayerStatus
import com.gracelink.android.data.db.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

/**
 * Database provider — seeds real data from assets on first access.
 *
 * BUG FIX: The previous version used RoomDatabase.Callback.onCreate to
 * seed, but INSTANCE was null when the callback fired (race condition).
 * Now we seed synchronously after the database is built, checking if
 * the content table is empty first.
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: GraceDatabase? = null
    @Volatile
    private var seeded = false

    fun get(context: Context): GraceDatabase = INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
            context.applicationContext,
            GraceDatabase::class.java,
            "gracelink.db"
        )
            .fallbackToDestructiveMigration()
            .build()
            .also { INSTANCE = it }
    }

    /**
     * Seeds the database from the bundled JSON asset if it hasn't been
     * seeded yet. Called from Application.onCreate.
     */
    suspend fun seedIfNeeded(context: Context) {
        if (seeded) return
        val db = get(context)
        // Check if content table is empty
        val count = db.contentDao().count()
        if (count == 0) {
            seedFromAsset(context.applicationContext, db)
        }
        seeded = true
    }

    private suspend fun seedFromAsset(context: Context, db: GraceDatabase) {
        try {
            val json = context.assets.open("gracelink_data.json").bufferedReader().use { it.readText() }
            val data = JSONObject(json)

            // Content — parse each item individually so one bad item
            // doesn't kill the entire batch
            val contentArr = data.getJSONArray("content")
            val contentItems = mutableListOf<ContentEntity>()
            for (i in 0 until contentArr.length()) {
                try {
                    val o = contentArr.getJSONObject(i)
                    contentItems.add(
                        ContentEntity(
                            id = o.getString("id"),
                            title = o.getString("title"),
                            description = o.optString("description", ""),
                            speaker = if (o.isNull("speaker")) null else o.optString("speaker"),
                            durationMs = o.optLong("durationMs", 0),
                            audioUrl = o.getString("audioUrl"),
                            type = ContentType.valueOf(o.optString("type", "PODCAST")),
                            language = ContentLanguage.valueOf(o.optString("language", "EN")),
                            category = ContentCategory.valueOf(o.optString("category", "TEACHING")),
                            thumbnailUrl = if (o.isNull("thumbnailUrl")) null else o.optString("thumbnailUrl"),
                            isDownloadable = o.optBoolean("isDownloadable", true),
                            publishedAt = o.optLong("publishedAt", 0),
                            isLive = o.optBoolean("isLive", false),
                            listenerCount = o.optInt("listenerCount", 0),
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.w("GraceLink", "Skipped content item $i: ${e.message}")
                }
            }
            if (contentItems.isNotEmpty()) db.contentDao().insertAll(contentItems)

            // Live sessions
            val sessionsArr = data.getJSONArray("liveSessions")
            val sessions = mutableListOf<LiveSessionEntity>()
            for (i in 0 until sessionsArr.length()) {
                try {
                    val o = sessionsArr.getJSONObject(i)
                    val hostsArr = o.getJSONArray("hosts")
                    val hostsJson = (0 until hostsArr.length()).joinToString(",") { "\"${hostsArr.getString(it)}\"" }
                    sessions.add(LiveSessionEntity(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        description = o.optString("description", ""),
                        hostsJson = "[$hostsJson]",
                        startTime = o.optLong("startTime"),
                        endTime = o.optLong("endTime"),
                        status = LiveSessionStatus.valueOf(o.optString("status", "UPCOMING")),
                        participantCount = o.optInt("participantCount", 0),
                        streamUrl = if (o.isNull("streamUrl")) null else o.optString("streamUrl"),
                        chatEnabled = o.optBoolean("chatEnabled", true),
                        language = ContentLanguage.valueOf(o.optString("language", "EN")),
                        category = ContentCategory.valueOf(o.optString("category", "DEBATES")),
                        coverImageUrl = if (o.isNull("coverImageUrl")) null else o.optString("coverImageUrl"),
                    ))
                } catch (e: Exception) {
                    android.util.Log.w("GraceLink", "Skipped session $i: ${e.message}")
                }
            }
            if (sessions.isNotEmpty()) db.liveSessionDao().insertAll(sessions)

            // Prayers
            val prayersArr = data.getJSONArray("prayers")
            val prayers = mutableListOf<PrayerEntity>()
            for (i in 0 until prayersArr.length()) {
                try {
                    val o = prayersArr.getJSONObject(i)
                    prayers.add(PrayerEntity(
                        id = o.getString("id"),
                        userId = if (o.isNull("userId")) null else o.optString("userId"),
                        displayName = if (o.isNull("displayName")) null else o.optString("displayName"),
                        text = o.getString("text"),
                        timestamp = o.optLong("timestamp"),
                        prayedCount = o.optInt("prayedCount", 0),
                        isAnswered = o.optBoolean("isAnswered", false),
                        isMine = o.optBoolean("isMine", false),
                        userPrayedThis = o.optBoolean("userPrayedThis", false),
                        status = PrayerStatus.valueOf(o.optString("status", "APPROVED")),
                        encouragementsJson = o.getJSONArray("encouragements").toString(),
                    ))
                } catch (e: Exception) {
                    android.util.Log.w("GraceLink", "Skipped prayer $i: ${e.message}")
                }
            }
            if (prayers.isNotEmpty()) db.prayerDao().insertAll(prayers)

            // Chat messages
            val chatsArr = data.getJSONArray("chatMessages")
            val chats = mutableListOf<ChatMessageEntity>()
            for (i in 0 until chatsArr.length()) {
                try {
                    val o = chatsArr.getJSONObject(i)
                    chats.add(ChatMessageEntity(
                        id = o.getString("id"),
                        sessionId = o.getString("sessionId"),
                        userId = if (o.isNull("userId")) null else o.optString("userId"),
                        displayName = o.getString("displayName"),
                        text = o.getString("text"),
                        timestamp = o.optLong("timestamp"),
                        isModerator = o.optBoolean("isModerator", false),
                        isHost = o.optBoolean("isHost", false),
                        isQuestion = o.optBoolean("isQuestion", false),
                        isMine = o.optBoolean("isMine", false),
                    ))
                } catch (e: Exception) {
                    android.util.Log.w("GraceLink", "Skipped chat $i: ${e.message}")
                }
            }
            if (chats.isNotEmpty()) db.chatDao().insertAll(chats)

            // User
            val userObj = data.getJSONObject("user")
            db.userDao().upsert(
                UserEntity(
                    uid = userObj.getString("uid"),
                    displayName = userObj.getString("displayName"),
                    email = userObj.getString("email"),
                    photoUrl = if (userObj.isNull("photoUrl")) null else userObj.optString("photoUrl"),
                    preferredLanguage = ContentLanguage.valueOf(userObj.optString("preferredLanguage", "EN")),
                    createdAt = userObj.optLong("createdAt"),
                    totalMinutes = userObj.optInt("totalMinutes"),
                    completedItems = userObj.optInt("completedItems"),
                    prayersOffered = userObj.optInt("prayersOffered"),
                    streakDays = userObj.optInt("streakDays"),
                    dataSaverEnabled = userObj.optBoolean("dataSaverEnabled"),
                    notificationsEnabled = userObj.optBoolean("notificationsEnabled"),
                )
            )

            // FM Schedule
            try {
                val scheduleJson = context.assets.open("fm_schedule.json").bufferedReader().use { it.readText() }
                val scheduleArr = JSONArray(scheduleJson)
                val scheduleItems = mutableListOf<FmScheduleEntity>()
                for (i in 0 until scheduleArr.length()) {
                    try {
                        val o = scheduleArr.getJSONObject(i)
                        val slot = o.getString("slot")
                        val startHour = slot.substring(0, 2).toInt()
                        val day = o.getString("day")
                        scheduleItems.add(FmScheduleEntity(
                            id = "${day}_${startHour}",  // composite key: "MON_6"
                            day = day,
                            timeSlot = slot,
                            startHour = startHour,
                            preacher = o.getString("preacher"),
                            description = o.optString("description", ""),
                            category = ContentCategory.valueOf(o.optString("category", "TEACHING")),
                        ))
                    } catch (e: Exception) {
                        android.util.Log.w("GraceLink", "Skipped FM slot $i: ${e.message}")
                    }
                }
                if (scheduleItems.isNotEmpty()) db.fmScheduleDao().insertAll(scheduleItems)
            } catch (_: Exception) { }

            android.util.Log.d("GraceLink", "Seeded ${contentItems.size} content, ${sessions.size} sessions, ${prayers.size} prayers, ${chats.size} chats from asset")
        } catch (e: Exception) {
            android.util.Log.e("GraceLink", "Failed to seed from asset", e)
        }
    }
}
