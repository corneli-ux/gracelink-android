# GraceLink — Android (MVP)

A modern Christian interactive radio app built per the **GraceLink Android Development Specification (MVP)**.

- **Platform:** Android (Kotlin + Jetpack Compose)
- **Min SDK:** 26 (Android 8.0) • **Target SDK:** 35
- **Architecture:** Clean Architecture + MVVM
- **Stack:** Compose + Material 3, Hilt, Navigation Compose (type-safe), Media3 ExoPlayer, Room, Retrofit + Kotlinx Serialization, Coil, StateFlow
- **Theme:** Dark slate `#0F172A` + gold accent `#F59E0B` + emerald success `#10B981` — per spec §4

---

## 1. What's inside

### Features (all spec §3 MVP must-haves)
- ✅ **Live radio streaming** — multi-channel HLS (Worship / Teaching / Regional Telugu) via ExoPlayer
- ✅ **On-demand library** — searchable, filterable by category / language / type
- ✅ **Full-featured player** — play/pause, ±10s/±30s skip, speed (0.5×–2×), sleep timer, favorites, background playback-ready
- ✅ **Offline downloads** — toggle UI in place; Room + WorkManager wiring TODO (see §5)
- ✅ **User accounts** — UI shell for sign-in (Firebase Auth not yet wired)
- ✅ **Text participation** — live chat panel with HOST / MOD / QUESTION badges
- ✅ **Prayer wall** — All / My / Answered tabs, I Prayed counter, Encourage, anonymous posting, moderation-ready (state=PENDING pattern called out in code)
- ✅ **Personalization** — greeting by time of day, Continue Listening, Recommended
- ⚙️ **Push notifications** — declared in manifest; FCM not yet wired (see §5)

### Screens (spec §4)
1. **Splash** — breathing cross + wordmark (1.5s) → Home
2. **Onboarding** — 3-page swipe (Listen • Participate • Belong) + EN/TE language chips + Sign-in CTAs
3. **Home / Discover** — greeting, Live Now banner, category chips, Continue Listening, Recommended
4. **Live Player** — large album art, seek bar, transport controls, speed picker sheet, sleep timer sheet, Favorite / Download, gold Join Conversation CTA
5. **Live Session** — session header + chat panel with HOST/MOD/Question bubbles and Question Mode toggle
6. **Library** — search + 3 filter rows (category / language / type) + content cards
7. **Events** — Live Now + Upcoming cards with Notify Me / Join Queue
8. **Prayer Wall** — tabbed list with prayer cards, bottom-sheet submit form, encouragement replies
9. **Profile** — stats grid (minutes/streak/saved/downloads), prayers-offered banner, settings (notifications, data saver, language), sign-out

### Design system (spec §4)
- `core/designsystem/theme/Color.kt` — full slate/gold/emerald palette + gradient brushes
- `core/designsystem/theme/Theme.kt` — Material 3 dark scheme (default) + light fallback
- `core/designsystem/theme/Type.kt` — Inter (body) + Lora (display) via Google Fonts provider
- `core/designsystem/theme/Shapes.kt` — 6/10/14/20/28 dp rounded-corner tokens
- `core/designsystem/components/` — GracePrimaryButton (gold gradient), GraceSecondaryButton, GraceCard, GraceSectionHeader, LiveBadge (pulsing)

### Architecture (spec §5)
```
com.gracelink.android/
├── GraceLinkApp.kt              @HiltAndroidApp Application
├── MainActivity.kt              single-activity host, edge-to-edge + splash
├── core/
│   ├── designsystem/            theme + reusable components
│   └── navigation/              type-safe routes + GraceLinkApp scaffold
├── data/
│   ├── model/                   ContentItem, LiveSession, PrayerRequest, User, ChatMessage
│   ├── repository/              ContentRepository, LiveSessionRepository, PrayerRepository, UserRepository
│   └── mock/                    MockData — fake seed for MVP demo
├── di/                          AppModule (Hilt)
├── player/                      GracePlayerController — ExoPlayer + StateFlow wrapper
└── feature/
    ├── splash/      onboarding/  home/
    ├── player/      (PlayerScreen + LiveSessionScreen + their ViewModels)
    ├── library/     events/      prayer/    profile/
```

---

## 2. Build it

### Requirements
- **Android Studio Ladybug (2024.2.1)+** or **Hedgehog** with AGP 8.7+
- **JDK 17** (bundled with Android Studio)
- Internet connection for first Gradle sync (downloads Compose BOM, Media3, Hilt, etc.)

### Open in Android Studio
1. Extract this zip to a folder.
2. Android Studio → **File → Open** → select the extracted folder.
3. When prompted, accept Gradle sync. The wrapper (`gradle/wrapper/gradle-wrapper.properties`) pins Gradle 8.10.2 — Android Studio will download it automatically.
4. Wait for indexing + first build (3–6 min on a cold cache).

### Run on a device / emulator
1. Connect an Android device (USB debugging on) or start an AVD (API 26+, recommended API 34+).
2. Select `app` configuration → click **Run** ▶.

### Build a debug APK from CLI
```bash
# Linux / macOS
./gradlew :app:assembleDebug

# Windows
gradlew.bat :app:assembleDebug
```
The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

### Build a release APK
```bash
./gradlew :app:assembleRelease
```
For MVP testing the release build is signed with the debug keystore (see `app/build.gradle.kts`). Replace with your own signing config before any public release.

---

## 3. Demo data

The app ships with in-memory mock data so you can navigate end-to-end without any backend:

- **3 live radio channels** (Worship 24/7, Living Word, Grace Telugu Radio)
- **8 on-demand items** (sermons, podcasts, debates — English + Telugu)
- **5 live sessions** (1 LIVE now + 4 upcoming)
- **6 prayer requests** (2 answered, 1 anonymous)
- **5 chat messages** seeded for the live session
- **1 demo user** "Cornelius" with stats

The live radio + on-demand items use a **public Google Sample HLS stream** (`https://storage.googleapis.com/wena-media-test/hls/master.m3u8`) so you can actually hear audio on day one. Swap with real GraceLink stream URLs in `MockData.kt` before launch.

---

## 4. What's NOT yet wired (callouts in code)

The MVP skeleton is intentionally complete on the UI layer; the following are stubbed with clear TODOs:

| Area | Status | Next step |
|---|---|---|
| **Firebase Auth** | UI only | Drop `google-services.json` into `/app`, uncomment the `google-services` plugin + Firebase deps in `app/build.gradle.kts`, wire `FirebaseAuth.signInWithEmailAndPassword` / Google sign-in into `UserRepository` |
| **Firestore content** | Mocked | Replace `ContentRepository.fetchHome()` and `searchLibrary()` with Firestore `QuerySnapshot` listeners |
| **Prayer moderation** | Submit inserts directly | Change `PrayerRepository.submitPrayer()` to write a Firestore doc with `status = "pending"` and add a Cloud Function for approval |
| **Live chat** | Local StateFlow | Replace `LiveSessionRepository.sendMessage` with a Firestore `messages` subcollection listener |
| **Background playback + notification** | ExoPlayer ready, no MediaSession | Implement a `MediaSessionService` and bind `GracePlayerController` to it (manifest already declares the service) |
| **Downloads (Room + WorkManager)** | UI toggles only | Add Room entities + a `CoroutineWorker` that calls ExoPlayer's `DownloadHelper` |
| **Push notifications (FCM)** | Manifest declared | Add `FirebaseMessagingService` + subscribe to live-event topics |
| **Real audio URLs** | Public sample HLS | Replace in `MockData.kt` |

---

## 5. Theming cheatsheet

| Token | Value | Where |
|---|---|---|
| Background | `#0F172A` | `Color.Slate950` |
| Card surface | `#1E293B` | `Color.Slate800` |
| Gold CTA | `#F59E0B` | `Color.Gold500` |
| Emerald success | `#10B981` | `Color.Emerald500` |
| Text primary | `#F8FAFC` | `Color.TextPrimary` |
| Text secondary | `#94A3B8` | `Color.TextSecondary` |
| LIVE pulse | `#FF3B3B` | `Color.LiveRed` |

All gradients live in `GraceGradients` (hero / gold / liveCard / prayerCard / playerScrim).

---

## 6. Telugu support

- All UI strings are duplicated in `res/values-te/strings.xml` (తెలుగు)
- Mock content includes 2 Telugu sermons + 1 Telugu live radio channel
- Language chip on Onboarding + Profile lets the user switch
- System font fallback handles Telugu script on Android 8+ (Noto Sans Telugu is bundled)

---

## 7. Known limitations / future work

- **No tests yet** — add Compose UI tests for each screen + unit tests for repositories/ViewModels before Play Store release
- **No ProGuard validation** — release minification is enabled but you should run a full smoke test on the release APK to catch any Hilt/Serialization keep-rule misses
- **No CI** — wire GitHub Actions to run `./gradlew assembleDebug lint test` on every PR
- **Phase 2 (per spec §3):** WebRTC voice participation (Agora/Daily), creator dashboard, multi-language UI, advanced recommendations, UPI donations

---

Built with excellence and care for the body of Christ.
