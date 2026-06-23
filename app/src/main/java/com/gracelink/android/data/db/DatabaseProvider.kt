package com.gracelink.android.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import org.json.JSONArray
import org.json.JSONObject

/**
 * Database provider with real data seeding from assets/gracelink_data.json.
 *
 * On first launch, the app reads 41 real podcast episodes (from iTunes),
 * 3 live radio channels, 3 live sessions, 6 prayers, 5 chat messages, and
 * 1 demo user from the bundled JSON asset and inserts them into Room.
 *
 * This is REAL data — real audio URLs, real titles, real artwork from
 * publicly available Christian podcasts (Timothy Keller, John MacArthur,
 * Alistair Begg, Paul Washer, etc.).
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: GraceDatabase? = null

    fun get(context: Context): GraceDatabase = INSTANCE ?: synchronized(this) {
        INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
    }

    private fun buildDatabase(context: Context): GraceDatabase {
        val appContext = context.applicationContext
        return Room.databaseBuilder(
            appContext,
            GraceDatabase::class.java,
            "gracelink.db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                        seedFromAsset(appContext, INSTANCE ?: return@launch)
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    private suspend fun seedFromAsset(context: Context, db: GraceDatabase) {
        try {
            val json = context.assets.open("gracelink_data.json").bufferedReader().use { it.readText() }
            val data = JSONObject(json)

            // Content
            val contentArr = data.getJSONArray("content")
            val contentItems = (0 until contentArr.length()).map { i ->
                val o = contentArr.getJSONObject(i)
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
            }
            db.contentDao().insertAll(contentItems)

            // Live sessions
            val sessionsArr = data.getJSONArray("liveSessions")
            val sessions = (0 until sessionsArr.length()).map { i ->
                val o = sessionsArr.getJSONObject(i)
                val hostsArr = o.getJSONArray("hosts")
                val hostsJson = (0 until hostsArr.length()).joinToString(",") { hostsArr.getString(it) }
                LiveSessionEntity(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    description = o.optString("description", ""),
                    hostsJson = "[${hostsJson}]",
                    startTime = o.optLong("startTime"),
                    endTime = o.optLong("endTime"),
                    status = LiveSessionStatus.valueOf(o.optString("status", "UPCOMING")),
                    participantCount = o.optInt("participantCount", 0),
                    streamUrl = if (o.isNull("streamUrl")) null else o.optString("streamUrl"),
                    chatEnabled = o.optBoolean("chatEnabled", true),
                    language = ContentLanguage.valueOf(o.optString("language", "EN")),
                    category = ContentCategory.valueOf(o.optString("category", "DEBATES")),
                    coverImageUrl = if (o.isNull("coverImageUrl")) null else o.optString("coverImageUrl"),
                )
            }
            db.liveSessionDao().insertAll(sessions)

            // Prayers
            val prayersArr = data.getJSONArray("prayers")
            val prayers = (0 until prayersArr.length()).map { i ->
                val o = prayersArr.getJSONObject(i)
                PrayerEntity(
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
                )
            }
            db.prayerDao().insertAll(prayers)

            // Chat messages
            val chatsArr = data.getJSONArray("chatMessages")
            val chats = (0 until chatsArr.length()).map { i ->
                val o = chatsArr.getJSONObject(i)
                ChatMessageEntity(
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
                )
            }
            db.chatDao().insertAll(chats)

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

            // FM Schedule (24/7 program slots with preachers)
            try {
                val scheduleJson = context.assets.open("fm_schedule.json").bufferedReader().use { it.readText() }
                val scheduleArr = JSONArray(scheduleJson)
                val scheduleItems = (0 until scheduleArr.length()).map { i ->
                    val o = scheduleArr.getJSONObject(i)
                    val slot = o.getString("slot")
                    val startHour = slot.substring(0, 2).toInt()
                    FmScheduleEntity(
                        day = o.getString("day"),
                        timeSlot = slot,
                        startHour = startHour,
                        preacher = o.getString("preacher"),
                        description = o.optString("description", ""),
                        category = ContentCategory.valueOf(o.optString("category", "TEACHING")),
                    )
                }
                db.fmScheduleDao().insertAll(scheduleItems)
            } catch (_: Exception) { }

            android.util.Log.d("GraceLink", "Seeded ${contentItems.size} content, ${sessions.size} sessions, ${prayers.size} prayers, ${chats.size} chats from asset")
        } catch (e: Exception) {
            android.util.Log.e("GraceLink", "Failed to seed from asset", e)
        }
    }
}
