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

    /**
     * v10 -> v11: FmScheduleEntity gained contentId/contentTitle (both
     * nullable) so a booked radio slot can link to real playable content.
     * This is the one schema change in this app's history I can write
     * with full confidence, since I made it directly. Earlier version
     * jumps still fall back to a destructive rebuild (see below) rather
     * than risk a wrong hand-written migration crashing the app -- but
     * from v10 onward, updates no longer wipe local data.
     */
    private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE fm_schedule ADD COLUMN contentId TEXT")
            db.execSQL("ALTER TABLE fm_schedule ADD COLUMN contentTitle TEXT")
        }
    }

    /** v11 -> v12: new questions/answers tables for the public Q&A forum. Brand new tables, so this is a plain CREATE TABLE -- no existing data at risk. */
    private val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `questions` (
                    `id` TEXT NOT NULL, `authorId` TEXT NOT NULL, `authorName` TEXT NOT NULL,
                    `title` TEXT NOT NULL, `body` TEXT NOT NULL, `createdAt` INTEGER NOT NULL,
                    `answerCount` INTEGER NOT NULL, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `answers` (
                    `id` TEXT NOT NULL, `questionId` TEXT NOT NULL, `authorId` TEXT NOT NULL,
                    `authorName` TEXT NOT NULL, `text` TEXT NOT NULL, `createdAt` INTEGER NOT NULL,
                    `replyToAnswerId` TEXT, `replyToAuthorName` TEXT, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    /** v12 -> v13: new collaboration_requests table. Brand new table, plain CREATE TABLE. */
    private val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `collaboration_requests` (
                    `id` TEXT NOT NULL, `fromUid` TEXT NOT NULL, `fromName` TEXT NOT NULL,
                    `fromType` TEXT NOT NULL, `toChurchId` TEXT NOT NULL, `toChurchName` TEXT NOT NULL,
                    `message` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    /**
     * v13 -> v14: full church administration -- roles, announcements,
     * groups + chat, direct messages, event RSVP, leadership, ministries,
     * service times, admin notes, moderation log. New columns on existing
     * tables use ALTER TABLE with defaults; everything else is a brand new
     * table (plain CREATE TABLE) -- no existing data at risk either way.
     */
    private val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE church_members ADD COLUMN role TEXT NOT NULL DEFAULT 'MEMBER'")
            db.execSQL("ALTER TABLE church_members ADD COLUMN adminNotes TEXT")
            db.execSQL("ALTER TABLE church_members ADD COLUMN phone TEXT")
            db.execSQL("ALTER TABLE church_members ADD COLUMN email TEXT")

            db.execSQL("ALTER TABLE churches ADD COLUMN vision TEXT")
            db.execSQL("ALTER TABLE churches ADD COLUMN serviceTimesJson TEXT")
            db.execSQL("ALTER TABLE churches ADD COLUMN address TEXT")
            db.execSQL("ALTER TABLE churches ADD COLUMN latitude REAL")
            db.execSQL("ALTER TABLE churches ADD COLUMN longitude REAL")

            db.execSQL("ALTER TABLE church_events ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE church_events ADD COLUMN recurrenceRule TEXT")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `announcements` (
                    `id` TEXT NOT NULL, `churchId` TEXT NOT NULL, `authorId` TEXT NOT NULL,
                    `authorName` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL,
                    `priority` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `expiresAt` INTEGER,
                    `targetRolesJson` TEXT, `isPinned` INTEGER NOT NULL, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `church_groups` (
                    `id` TEXT NOT NULL, `churchId` TEXT NOT NULL, `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL, `type` TEXT NOT NULL, `leaderUserId` TEXT,
                    `leaderName` TEXT, `memberCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL,
                    `isPrivate` INTEGER NOT NULL, `coverImageUrl` TEXT, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `group_members` (
                    `id` TEXT NOT NULL, `groupId` TEXT NOT NULL, `churchId` TEXT NOT NULL,
                    `userId` TEXT NOT NULL, `displayName` TEXT NOT NULL, `role` TEXT NOT NULL,
                    `joinedAt` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `event_rsvps` (
                    `id` TEXT NOT NULL, `eventId` TEXT NOT NULL, `churchId` TEXT NOT NULL,
                    `userId` TEXT NOT NULL, `displayName` TEXT NOT NULL, `status` TEXT NOT NULL,
                    `respondedAt` INTEGER NOT NULL, `note` TEXT, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `leadership_team` (
                    `id` TEXT NOT NULL, `churchId` TEXT NOT NULL, `userId` TEXT NOT NULL,
                    `displayName` TEXT NOT NULL, `title` TEXT NOT NULL, `bio` TEXT,
                    `photoUrl` TEXT, `sortOrder` INTEGER NOT NULL, `isPublic` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `ministries` (
                    `id` TEXT NOT NULL, `churchId` TEXT NOT NULL, `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL, `leaderUserId` TEXT, `leaderName` TEXT,
                    `meetingInfo` TEXT, `coverImageUrl` TEXT, `sortOrder` INTEGER NOT NULL,
                    `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `group_messages` (
                    `id` TEXT NOT NULL, `groupId` TEXT NOT NULL, `churchId` TEXT NOT NULL,
                    `senderId` TEXT NOT NULL, `senderName` TEXT NOT NULL, `text` TEXT NOT NULL,
                    `type` TEXT NOT NULL, `mediaUrl` TEXT, `createdAt` INTEGER NOT NULL,
                    `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `direct_messages` (
                    `id` TEXT NOT NULL, `churchId` TEXT NOT NULL, `conversationId` TEXT NOT NULL,
                    `senderId` TEXT NOT NULL, `senderName` TEXT NOT NULL, `receiverId` TEXT NOT NULL,
                    `text` TEXT NOT NULL, `type` TEXT NOT NULL, `mediaUrl` TEXT,
                    `createdAt` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `service_times` (
                    `id` TEXT NOT NULL, `churchId` TEXT NOT NULL, `dayOfWeek` INTEGER NOT NULL,
                    `time` TEXT NOT NULL, `name` TEXT NOT NULL, `location` TEXT,
                    `isOnline` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `admin_notes` (
                    `id` TEXT NOT NULL, `churchId` TEXT NOT NULL, `memberUserId` TEXT NOT NULL,
                    `authorId` TEXT NOT NULL, `authorName` TEXT NOT NULL, `note` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL, `isPrivate` INTEGER NOT NULL, PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `moderation_actions` (
                    `id` TEXT NOT NULL, `churchId` TEXT NOT NULL, `actorId` TEXT NOT NULL,
                    `actorName` TEXT NOT NULL, `targetUserId` TEXT, `targetContentId` TEXT,
                    `action` TEXT NOT NULL, `reason` TEXT NOT NULL, `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    fun get(context: Context): GraceDatabase = INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
            context.applicationContext,
            GraceDatabase::class.java,
            "gracelink.db"
        )
            .addMigrations(MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
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

            // NOTE: deliberately NOT seeding a demo UserEntity here anymore.
            // A fake pre-existing "you" profile was satisfying the app's
            // "has this person set up their real profile yet" check on every
            // fresh install, which made Registration get skipped entirely --
            // Google sign-in would drop straight into the app because the
            // seeded demo user already looked like a completed signup.
            // Real profiles now only ever come from Auth + Registration.

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
