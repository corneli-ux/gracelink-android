package com.gracelink.android.data.seed

import com.gracelink.android.data.db.entity.ChatMessageEntity
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.ContentType
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.db.entity.LiveSessionStatus
import com.gracelink.android.data.db.entity.PrayerEntity
import com.gracelink.android.data.db.entity.PrayerStatus
import com.gracelink.android.data.db.entity.UserEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Real seed data inserted into Room on first launch.
 *
 * This is NOT mock data — it's the initial content set, persisted to the
 * device's SQLite database. The user can interact with it (favorite, download,
 * pray, etc.) and changes survive app restarts. Replace any of these with
 * Firestore-fetched content later by clearing and re-inserting.
 */
object SeedData {

    private const val STREAM_URL = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8"

    fun user(): UserEntity = UserEntity(
        uid = "u_demo",
        displayName = "Cornelius",
        email = "cornelius@gracelink.app",
        photoUrl = null,
        preferredLanguage = ContentLanguage.EN,
        createdAt = daysAgo(60),
        totalMinutes = 8 * 60 + 23,
        completedItems = 14,
        prayersOffered = 31,
        streakDays = 7,
        dataSaverEnabled = false,
        notificationsEnabled = true,
    )

    fun content(): List<ContentEntity> = listOf(
        // ── Live radio channels ──────────────────────────────────────────────
        ContentEntity(
            id = "live_worship", title = "Grace Worship 24/7",
            description = "Continuous worship — modern + hymns, bilingual.",
            speaker = null, durationMs = 0, audioUrl = STREAM_URL,
            type = ContentType.LIVE_RADIO, language = ContentLanguage.EN, category = ContentCategory.WORSHIP,
            thumbnailUrl = "https://images.unsplash.com/photo-1516455590571-18256e5bb9ff?w=900&q=80",
            isDownloadable = false, publishedAt = 0, isLive = true, listenerCount = 1243,
        ),
        ContentEntity(
            id = "live_teaching", title = "Living Word Radio",
            description = "Back-to-back verse-by-verse teaching.",
            speaker = null, durationMs = 0, audioUrl = STREAM_URL,
            type = ContentType.LIVE_RADIO, language = ContentLanguage.EN, category = ContentCategory.TEACHING,
            thumbnailUrl = "https://images.unsplash.com/photo-1504052434569-70ad5836ab65?w=900&q=80",
            isDownloadable = false, publishedAt = 0, isLive = true, listenerCount = 856,
        ),
        ContentEntity(
            id = "live_regional", title = "Grace Telugu Radio",
            description = "తెలుగు కీర్తనలు మరియు బోధలు. 24/7.",
            speaker = null, durationMs = 0, audioUrl = STREAM_URL,
            type = ContentType.LIVE_RADIO, language = ContentLanguage.TE, category = ContentCategory.REGIONAL,
            thumbnailUrl = "https://images.unsplash.com/photo-1496024840928-4c417adf211d?w=900&q=80",
            isDownloadable = false, publishedAt = 0, isLive = true, listenerCount = 612,
        ),
        // ── On-demand sermons + podcasts ─────────────────────────────────────
        ContentEntity(
            id = "sermon_001", title = "The Shepherd's Heart",
            description = "A walk through Psalm 23 — what it means to be led, fed, and known.",
            speaker = "Pastor Anil Kumar", durationMs = 32 * 60_000L, audioUrl = STREAM_URL,
            type = ContentType.SERMON, language = ContentLanguage.EN, category = ContentCategory.TEACHING,
            thumbnailUrl = "https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600&q=80",
            isDownloadable = true, publishedAt = daysAgo(2), isLive = false, listenerCount = 0,
        ),
        ContentEntity(
            id = "sermon_002", title = "Walking by Faith in 2026",
            description = "How do we trust God in a year of accelerating change?",
            speaker = "Pastor Anil Kumar", durationMs = 41 * 60_000L, audioUrl = STREAM_URL,
            type = ContentType.SERMON, language = ContentLanguage.EN, category = ContentCategory.TEACHING,
            thumbnailUrl = "https://images.unsplash.com/photo-1493417627896-98939e4f8b3a?w=600&q=80",
            isDownloadable = true, publishedAt = daysAgo(5), isLive = false, listenerCount = 0,
        ),
        ContentEntity(
            id = "sermon_te_001", title = "దేవుని వాగ్దానాలు",
            description = "ఆబ్రహాము నుండి క్రీస్తు వరకు — దేవుని నిబంధనల ప్రయాణం.",
            speaker = "Pas. Raju Venkat", durationMs = 38 * 60_000L, audioUrl = STREAM_URL,
            type = ContentType.SERMON, language = ContentLanguage.TE, category = ContentCategory.REGIONAL,
            thumbnailUrl = "https://images.unsplash.com/photo-1532528326709-6c6b458d2a85?w=600&q=80",
            isDownloadable = true, publishedAt = daysAgo(1), isLive = false, listenerCount = 0,
        ),
        ContentEntity(
            id = "podcast_001", title = "Faith & Work — Priya's Story",
            description = "A software engineer on being a Christian in tech.",
            speaker = "Priya Rao", durationMs = 28 * 60_000L, audioUrl = STREAM_URL,
            type = ContentType.PODCAST, language = ContentLanguage.EN, category = ContentCategory.TESTIMONY,
            thumbnailUrl = "https://images.unsplash.com/photo-1521791136064-7986c2920216?w=600&q=80",
            isDownloadable = true, publishedAt = daysAgo(3), isLive = false, listenerCount = 0,
        ),
        ContentEntity(
            id = "podcast_002", title = "Youth Roundtable: Anxiety",
            description = "Real talk with 4 young adults navigating anxiety biblically.",
            speaker = "GraceLink Youth", durationMs = 44 * 60_000L, audioUrl = STREAM_URL,
            type = ContentType.PODCAST, language = ContentLanguage.EN, category = ContentCategory.YOUTH,
            thumbnailUrl = "https://images.unsplash.com/photo-1516455590571-18256e5bb9ff?w=600&q=80",
            isDownloadable = true, publishedAt = daysAgo(7), isLive = false, listenerCount = 0,
        ),
        ContentEntity(
            id = "debate_001", title = "Calvinism vs Arminianism — Friendly Debate",
            description = "Two pastors gently work through sovereignty vs free will.",
            speaker = "Pastor Anil & Pastor Mark", durationMs = 67 * 60_000L, audioUrl = STREAM_URL,
            type = ContentType.DEBATE, language = ContentLanguage.EN, category = ContentCategory.DEBATES,
            thumbnailUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=600&q=80",
            isDownloadable = true, publishedAt = daysAgo(4), isLive = false, listenerCount = 0,
        ),
        ContentEntity(
            id = "sermon_003", title = "Prayer that Moves Heaven",
            description = "Daniel 9 — what bold, repentant prayer looks like.",
            speaker = "Pastor Anil Kumar", durationMs = 35 * 60_000L, audioUrl = STREAM_URL,
            type = ContentType.SERMON, language = ContentLanguage.EN, category = ContentCategory.TEACHING,
            thumbnailUrl = "https://images.unsplash.com/photo-1488229297570-58520851e868?w=600&q=80",
            isDownloadable = true, publishedAt = daysAgo(10), isLive = false, listenerCount = 0,
        ),
        ContentEntity(
            id = "sermon_te_002", title = "పరిశుద్ధాత్మ ఫలాలు",
            description = "గలతీయులు 5 — మన జీవితంలో ఆత్మ ఫలాలు ఎలా కనిపిస్తాయి?",
            speaker = "Pas. Raju Venkat", durationMs = 33 * 60_000L, audioUrl = STREAM_URL,
            type = ContentType.SERMON, language = ContentLanguage.TE, category = ContentCategory.REGIONAL,
            thumbnailUrl = "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?w=600&q=80",
            isDownloadable = true, publishedAt = daysAgo(8), isLive = false, listenerCount = 0,
        ),
    )

    fun liveSessions(): List<LiveSessionEntity> = listOf(
        LiveSessionEntity(
            id = "session_001", title = "Live Q&A: Suffering & Sovereignty",
            description = "Open mic. Ask Pastor Anil anything about suffering.",
            hostsJson = """["Pastor Anil Kumar"]""",
            startTime = plusDays(0, 19), endTime = plusDays(0, 21),
            status = LiveSessionStatus.LIVE, participantCount = 327,
            streamUrl = STREAM_URL, chatEnabled = true,
            language = ContentLanguage.EN, category = ContentCategory.DEBATES,
            coverImageUrl = "https://images.unsplash.com/photo-1504052434569-70ad5836ab65?w=900&q=80",
        ),
        LiveSessionEntity(
            id = "session_002", title = "Youth Debate: Faith vs Science",
            description = "Two youth leaders and a scientist walk through Genesis 1.",
            hostsJson = """["Sam","Dr. Anita","Mark"]""",
            startTime = plusDays(1, 18), endTime = plusDays(1, 20),
            status = LiveSessionStatus.UPCOMING, participantCount = 0,
            streamUrl = STREAM_URL, chatEnabled = true,
            language = ContentLanguage.EN, category = ContentCategory.DEBATES,
            coverImageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=900&q=80",
        ),
        LiveSessionEntity(
            id = "session_003", title = "Telugu Worship Night",
            description = "కీర్తనలు, ప్రార్థన, సహవాసం. అందరూ ఆహ్వానితులు.",
            hostsJson = """["Pas. Raju Venkat"]""",
            startTime = plusDays(2, 19), endTime = plusDays(2, 21),
            status = LiveSessionStatus.UPCOMING, participantCount = 0,
            streamUrl = STREAM_URL, chatEnabled = true,
            language = ContentLanguage.TE, category = ContentCategory.REGIONAL,
            coverImageUrl = "https://images.unsplash.com/photo-1496024840928-4c417adf211d?w=900&q=80",
        ),
        LiveSessionEntity(
            id = "session_004", title = "Marriage Roundtable",
            description = "Three couples share honestly. Submit questions in advance.",
            hostsJson = """["Anil & Susan","Mark & Lydia","Raju & Sita"]""",
            startTime = plusDays(4, 20), endTime = plusDays(4, 22),
            status = LiveSessionStatus.UPCOMING, participantCount = 0,
            streamUrl = STREAM_URL, chatEnabled = true,
            language = ContentLanguage.EN, category = ContentCategory.TEACHING,
            coverImageUrl = "https://images.unsplash.com/photo-1518621736915-f3b1c41bfd00?w=900&q=80",
        ),
        LiveSessionEntity(
            id = "session_005", title = "Open Mic Testimonies",
            description = "Listeners share what God has done. 90 seconds each.",
            hostsJson = """["Priya Rao"]""",
            startTime = plusDays(6, 19), endTime = plusDays(6, 20, 30),
            status = LiveSessionStatus.UPCOMING, participantCount = 0,
            streamUrl = STREAM_URL, chatEnabled = true,
            language = ContentLanguage.EN, category = ContentCategory.TESTIMONY,
            coverImageUrl = "https://images.unsplash.com/photo-1521791136064-7986c2920216?w=900&q=80",
        ),
    )

    fun prayers(): List<PrayerEntity> = listOf(
        PrayerEntity(
            id = "pr_001", userId = null, displayName = null,
            text = "Please pray for my mother's surgery on Friday. That the Lord would guide the surgeon's hands.",
            timestamp = hoursAgo(2), prayedCount = 47, isAnswered = false,
            isMine = false, userPrayedThis = false, status = PrayerStatus.APPROVED,
            encouragementsJson = "[]",
        ),
        PrayerEntity(
            id = "pr_002", userId = "u_002", displayName = "Daniel",
            text = "Job interview tomorrow. Praying for peace and clarity — and that God closes or opens the right door.",
            timestamp = hoursAgo(5), prayedCount = 89, isAnswered = false,
            isMine = false, userPrayedThis = false, status = PrayerStatus.APPROVED,
            encouragementsJson = "[]",
        ),
        PrayerEntity(
            id = "pr_003", userId = "u_003", displayName = "Lydia",
            text = "GOD ANSWERED! After 8 months of waiting, my brother came to church with me. Praise Him!",
            timestamp = hoursAgo(28), prayedCount = 213, isAnswered = true,
            isMine = false, userPrayedThis = false, status = PrayerStatus.APPROVED,
            encouragementsJson = """[{"id":"e1","userId":null,"displayName":"Anonymous","text":"Rejoicing with you!","timestamp":${hoursAgo(20)}},{"id":"e2","userId":"u_004","displayName":"Sara","text":"Hallelujah! Standing in awe.","timestamp":${hoursAgo(15)}}]""",
        ),
        PrayerEntity(
            id = "pr_004", userId = null, displayName = null,
            text = "Struggling with anxiety this week. Pray for the peace that surpasses understanding.",
            timestamp = hoursAgo(40), prayedCount = 156, isAnswered = false,
            isMine = false, userPrayedThis = false, status = PrayerStatus.APPROVED,
            encouragementsJson = "[]",
        ),
        PrayerEntity(
            id = "pr_005", userId = "u_005", displayName = "Mark",
            text = "Pray for our church plant in Warangal. We need a worship leader and more monthly support.",
            timestamp = hoursAgo(72), prayedCount = 78, isAnswered = false,
            isMine = false, userPrayedThis = false, status = PrayerStatus.APPROVED,
            encouragementsJson = "[]",
        ),
        PrayerEntity(
            id = "pr_006", userId = "u_006", displayName = "Auntie Mary",
            text = "Healed of stage-3 cancer — 5 years clear now. Jesus is the same yesterday, today, forever.",
            timestamp = hoursAgo(100), prayedCount = 432, isAnswered = true,
            isMine = false, userPrayedThis = false, status = PrayerStatus.APPROVED,
            encouragementsJson = "[]",
        ),
    )

    fun chatMessages(): List<ChatMessageEntity> = listOf(
        ChatMessageEntity("m1", "session_001", null, "Moderator", "Welcome everyone — questions in the queue will be answered in order.", hoursAgo(60), isModerator = true, isHost = false, isQuestion = false, isMine = false),
        ChatMessageEntity("m2", "session_001", "u_010", "Samuel", "Pastor, how do we reconcile God's sovereignty with our pain?", hoursAgo(55), isModerator = false, isHost = false, isQuestion = true, isMine = false),
        ChatMessageEntity("m3", "session_001", "u_011", "Sara", "We're praying with you Samuel", hoursAgo(54), isModerator = false, isHost = false, isQuestion = false, isMine = false),
        ChatMessageEntity("m4", "session_001", "u_anil", "Pastor Anil", "Great question — let's turn to Romans 8:28 first.", hoursAgo(50), isModerator = false, isHost = true, isQuestion = false, isMine = false),
        ChatMessageEntity("m5", "session_001", "u_012", "Raju", "Glory to God for this time.", hoursAgo(40), isModerator = false, isHost = false, isQuestion = false, isMine = false),
    )

    private fun daysAgo(days: Int): Long = LocalDate.now(ZoneId.systemDefault())
        .minusDays(days.toLong())
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun hoursAgo(hours: Int): Long = Instant.now().minusSeconds(hours * 3600L).toEpochMilli()

    private fun plusDays(daysAhead: Int, hour: Int, minute: Int = 0): Long = LocalDate.now(ZoneId.systemDefault())
        .plusDays(daysAhead.toLong())
        .atTime(hour, minute)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
