package com.gracelink.android.data.mock

import com.gracelink.android.data.model.ChatMessage
import com.gracelink.android.data.model.ContentCategory
import com.gracelink.android.data.model.ContentItem
import com.gracelink.android.data.model.ContentLanguage
import com.gracelink.android.data.model.ContentType
import com.gracelink.android.data.model.Encouragement
import com.gracelink.android.data.model.LiveSession
import com.gracelink.android.data.model.LiveSessionStatus
import com.gracelink.android.data.model.PrayerRequest
import com.gracelink.android.data.model.User
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * In-memory seed data for the MVP.
 *
 * Per spec §11 step 7: "Add Home screen with fake data, then connect to Firestore."
 * This object is the single source of fake data until Firestore is wired in —
 * swap any repository to call Firestore and these mocks become irrelevant.
 *
 * Audio URLs point at the public Google Sample HLS stream so the player can
 * actually play something on day one. Replace with real GraceLink streams.
 */
object MockData {

    // ── Live radio channels (always-on HLS) ─────────────────────────────────
    val liveRadioChannels: List<ContentItem> = listOf(
        ContentItem(
            id = "live_worship",
            title = "Grace Worship 24/7",
            description = "Continuous worship — modern + hymns, bilingual.",
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.LIVE_RADIO,
            language = ContentLanguage.EN,
            category = ContentCategory.WORSHIP,
            thumbnailUrl = "https://images.unsplash.com/photo-1516455590571-18256e5bb9ff?w=900&q=80",
            isLive = true,
            listenerCount = 1243,
        ),
        ContentItem(
            id = "live_teaching",
            title = "Living Word Radio",
            description = "Back-to-back verse-by-verse teaching.",
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.LIVE_RADIO,
            language = ContentLanguage.EN,
            category = ContentCategory.TEACHING,
            thumbnailUrl = "https://images.unsplash.com/photo-1504052434569-70ad5836ab65?w=900&q=80",
            isLive = true,
            listenerCount = 856,
        ),
        ContentItem(
            id = "live_regional",
            title = "Grace Telugu Radio",
            description = "తెలుగు కీర్తనలు మరియు బోధలు. 24/7.",
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.LIVE_RADIO,
            language = ContentLanguage.TE,
            category = ContentCategory.REGIONAL,
            thumbnailUrl = "https://images.unsplash.com/photo-1496024840928-4c417adf211d?w=900&q=80",
            isLive = true,
            listenerCount = 612,
        ),
    )

    // ── On-demand sermons + podcasts ────────────────────────────────────────
    val onDemandLibrary: List<ContentItem> = listOf(
        ContentItem(
            id = "sermon_001",
            title = "The Shepherd's Heart",
            description = "A walk through Psalm 23 — what it means to be led, fed, and known.",
            speaker = "Pastor Anil Kumar",
            durationMs = 32 * 60_000L,
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.SERMON,
            language = ContentLanguage.EN,
            category = ContentCategory.TEACHING,
            thumbnailUrl = "https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600&q=80",
            publishedAt = daysAgo(2),
        ),
        ContentItem(
            id = "sermon_002",
            title = "Walking by Faith in 2026",
            description = "How do we trust God in a year of accelerating change?",
            speaker = "Pastor Anil Kumar",
            durationMs = 41 * 60_000L,
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.SERMON,
            language = ContentLanguage.EN,
            category = ContentCategory.TEACHING,
            thumbnailUrl = "https://images.unsplash.com/photo-1493417627896-98939e4f8b3a?w=600&q=80",
            publishedAt = daysAgo(5),
        ),
        ContentItem(
            id = "sermon_te_001",
            title = "దేవుని వాగ్దానాలు",
            description = "ఆబ్రహాము నుండి క్రీస్తు వరకు — దేవుని నిబంధనల ప్రయాణం.",
            speaker = "Pas. Raju Venkat",
            durationMs = 38 * 60_000L,
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.SERMON,
            language = ContentLanguage.TE,
            category = ContentCategory.REGIONAL,
            thumbnailUrl = "https://images.unsplash.com/photo-1532528326709-6c6b458d2a85?w=600&q=80",
            publishedAt = daysAgo(1),
        ),
        ContentItem(
            id = "podcast_001",
            title = "Faith & Work — Priya's Story",
            description = "A software engineer on being a Christian in tech.",
            speaker = "Priya Rao",
            durationMs = 28 * 60_000L,
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.PODCAST,
            language = ContentLanguage.EN,
            category = ContentCategory.TESTIMONY,
            thumbnailUrl = "https://images.unsplash.com/photo-1521791136064-7986c2920216?w=600&q=80",
            publishedAt = daysAgo(3),
        ),
        ContentItem(
            id = "podcast_002",
            title = "Youth Roundtable: Anxiety",
            description = "Real talk with 4 young adults navigating anxiety biblically.",
            speaker = "GraceLink Youth",
            durationMs = 44 * 60_000L,
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.PODCAST,
            language = ContentLanguage.EN,
            category = ContentCategory.YOUTH,
            thumbnailUrl = "https://images.unsplash.com/photo-1516455590571-18256e5bb9ff?w=600&q=80",
            publishedAt = daysAgo(7),
        ),
        ContentItem(
            id = "debate_001",
            title = "Calvinism vs Arminianism — Friendly Debate",
            description = "Two pastors gently work through sovereignty vs free will.",
            speaker = "Pastor Anil & Pastor Mark",
            durationMs = 67 * 60_000L,
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.DEBATE,
            language = ContentLanguage.EN,
            category = ContentCategory.DEBATES,
            thumbnailUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=600&q=80",
            publishedAt = daysAgo(4),
        ),
        ContentItem(
            id = "sermon_003",
            title = "Prayer that Moves Heaven",
            description = "Daniel 9 — what bold, repentant prayer looks like.",
            speaker = "Pastor Anil Kumar",
            durationMs = 35 * 60_000L,
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.SERMON,
            language = ContentLanguage.EN,
            category = ContentCategory.TEACHING,
            thumbnailUrl = "https://images.unsplash.com/photo-1488229297570-58520851e868?w=600&q=80",
            publishedAt = daysAgo(10),
        ),
        ContentItem(
            id = "sermon_te_002",
            title = "పరిశుద్ధాత్మ ఫలాలు",
            description = "గలతీయులు 5 — మన జీవితంలో ఆత్మ ఫలాలు ఎలా కనిపిస్తాయి?",
            speaker = "Pas. Raju Venkat",
            durationMs = 33 * 60_000L,
            audioUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            type = ContentType.SERMON,
            language = ContentLanguage.TE,
            category = ContentCategory.REGIONAL,
            thumbnailUrl = "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?w=600&q=80",
            publishedAt = daysAgo(8),
        ),
    )

    // ── Live sessions (debates / Q&A / worship nights) ───────────────────────
    val liveSessions: List<LiveSession> = listOf(
        LiveSession(
            id = "session_001",
            title = "Live Q&A: Suffering & Sovereignty",
            description = "Open mic. Ask Pastor Anil anything about suffering.",
            hosts = listOf("Pastor Anil Kumar"),
            startTime = plusDays(0, hour = 19),
            endTime = plusDays(0, hour = 21),
            status = LiveSessionStatus.LIVE,
            participantCount = 327,
            streamUrl = "https://storage.googleapis.com/wena-media-test/hls/master.m3u8",
            chatEnabled = true,
            coverImageUrl = "https://images.unsplash.com/photo-1504052434569-70ad5836ab65?w=900&q=80",
        ),
        LiveSession(
            id = "session_002",
            title = "Youth Debate: Faith vs Science",
            description = "Two youth leaders and a scientist walk through Genesis 1.",
            hosts = listOf("Sam", "Dr. Anita", "Mark"),
            startTime = plusDays(1, hour = 18),
            endTime = plusDays(1, hour = 20),
            status = LiveSessionStatus.UPCOMING,
            participantCount = 0,
            chatEnabled = true,
            coverImageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=900&q=80",
            remindMe = false,
        ),
        LiveSession(
            id = "session_003",
            title = "Telugu Worship Night",
            description = "కీర్తనలు, ప్రార్థన, సహవాసం. అందరూ ఆహ్వానితులు.",
            hosts = listOf("Pas. Raju Venkat"),
            startTime = plusDays(2, hour = 19),
            endTime = plusDays(2, hour = 21),
            status = LiveSessionStatus.UPCOMING,
            participantCount = 0,
            chatEnabled = true,
            language = ContentLanguage.TE,
            coverImageUrl = "https://images.unsplash.com/photo-1496024840928-4c417adf211d?w=900&q=80",
        ),
        LiveSession(
            id = "session_004",
            title = "Marriage Roundtable",
            description = "Three couples share honestly. Submit questions in advance.",
            hosts = listOf("Anil & Susan", "Mark & Lydia", "Raju & Sita"),
            startTime = plusDays(4, hour = 20),
            endTime = plusDays(4, hour = 22),
            status = LiveSessionStatus.UPCOMING,
            chatEnabled = true,
            coverImageUrl = "https://images.unsplash.com/photo-1518621736915-f3b1c41bfd00?w=900&q=80",
        ),
        LiveSession(
            id = "session_005",
            title = "Open Mic Testimonies",
            description = "Listeners share what God has done. 90 seconds each.",
            hosts = listOf("Priya Rao"),
            startTime = plusDays(6, hour = 19),
            endTime = plusDays(6, hour = 20, minute = 30),
            status = LiveSessionStatus.UPCOMING,
            chatEnabled = true,
            coverImageUrl = "https://images.unsplash.com/photo-1521791136064-7986c2920216?w=900&q=80",
        ),
    )

    // ── Prayer wall seed ─────────────────────────────────────────────────────
    val prayerRequests: List<PrayerRequest> = listOf(
        PrayerRequest(
            id = "pr_001",
            userId = null,
            displayName = null,
            text = "Please pray for my mother's surgery on Friday. That the Lord would guide the surgeon's hands.",
            timestamp = hoursAgo(2),
            prayedCount = 47,
            isAnswered = false,
        ),
        PrayerRequest(
            id = "pr_002",
            userId = "u_002",
            displayName = "Daniel",
            text = "Job interview tomorrow. Praying for peace and clarity — and that God closes or opens the right door.",
            timestamp = hoursAgo(5),
            prayedCount = 89,
            isAnswered = false,
        ),
        PrayerRequest(
            id = "pr_003",
            userId = "u_003",
            displayName = "Lydia",
            text = "GOD ANSWERED! After 8 months of waiting, my brother came to church with me. Praise Him!",
            timestamp = hoursAgo(28),
            prayedCount = 213,
            isAnswered = true,
            encouragements = listOf(
                Encouragement("e1", null, "Anonymous", "Rejoicing with you!", hoursAgo(20)),
                Encouragement("e2", "u_004", "Sara", "Hallelujah! Standing in awe.", hoursAgo(15)),
            ),
        ),
        PrayerRequest(
            id = "pr_004",
            userId = null,
            displayName = null,
            text = "Struggling with anxiety this week. Pray for the peace that surpasses understanding.",
            timestamp = hoursAgo(40),
            prayedCount = 156,
            isAnswered = false,
        ),
        PrayerRequest(
            id = "pr_005",
            userId = "u_005",
            displayName = "Mark",
            text = "Pray for our church plant in Warangal. We need a worship leader and $2k/month more support.",
            timestamp = hoursAgo(72),
            prayedCount = 78,
            isAnswered = false,
        ),
        PrayerRequest(
            id = "pr_006",
            userId = "u_006",
            displayName = "Auntie Mary",
            text = "Healed of stage-3 cancer — 5 years clear now. Jesus is the same yesterday, today, forever.",
            timestamp = hoursAgo(100),
            prayedCount = 432,
            isAnswered = true,
        ),
    )

    // ── Chat seed for the live session ───────────────────────────────────────
    val liveChatMessages: List<ChatMessage> = listOf(
        ChatMessage("m1", "session_001", null, "Moderator", "Welcome everyone — questions in the queue will be answered in order.", hoursAgo(60), isModerator = true),
        ChatMessage("m2", "session_001", "u_010", "Samuel", "Pastor, how do we reconcile God's sovereignty with our pain?", hoursAgo(55), isQuestion = true),
        ChatMessage("m3", "session_001", "u_011", "Sara", "We're praying with you Samuel 🙏", hoursAgo(54)),
        ChatMessage("m4", "session_001", "u_anil", "Pastor Anil", "Great question — let's turn to Romans 8:28 first.", hoursAgo(50), isHost = true),
        ChatMessage("m5", "session_001", "u_012", "Raju", "Glory to God for this time.", hoursAgo(40)),
    )

    // ── Currently signed-in (mock) user ──────────────────────────────────────
    val currentUser: User = User(
        uid = "u_demo",
        displayName = "Cornelius",
        email = "cornelius@gracelink.app",
        photoUrl = null,
        preferredLanguage = ContentLanguage.EN,
        createdAt = daysAgo(60),
        listeningStats = com.gracelink.android.data.model.ListeningStats(
            totalMinutes = 8 * 60 + 23,
            completedItems = 14,
            prayersOffered = 31,
            streakDays = 7,
        ),
        dataSaverEnabled = false,
        notificationsEnabled = true,
    )

    // ── "Continue listening" mock ────────────────────────────────────────────
    val continueListening: List<Pair<ContentItem, Long>> = listOf(
        onDemandLibrary[0] to (12 * 60_000L),
        onDemandLibrary[3] to (4 * 60_000L),
        onDemandLibrary[5] to (28 * 60_000L),
    )

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun daysAgo(days: Int): Long = LocalDate.now(ZoneId.systemDefault())
        .minusDays(days.toLong())
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    private fun hoursAgo(hours: Int): Long = Instant.now()
        .minusSeconds(hours * 3600L)
        .toEpochMilli()

    private fun plusDays(daysAhead: Int, hour: Int, minute: Int = 0): Long = LocalDate.now(ZoneId.systemDefault())
        .plusDays(daysAhead.toLong())
        .atTime(hour, minute)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
