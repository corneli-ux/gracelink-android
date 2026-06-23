#!/usr/bin/env python3
"""Rebuild gracelink_data.json with filtered real episodes + FM schedule + live sessions."""
import json, time, os

with open('/home/z/my-project/scripts/real-episodes.json') as f:
    episodes = json.load(f)

now = int(time.time() * 1000)
stream_url = "https://stream.zeno.fm/0r0xa792kwzuv"

data = {"content": [], "liveSessions": [], "prayers": [], "chatMessages": [], "user": None}

# Add real episodes
for ep in episodes:
    data["content"].append({
        "id": ep["id"], "title": ep["title"], "description": ep.get("description", ""),
        "speaker": ep.get("speaker", ""), "durationMs": ep.get("durationMs", 0),
        "audioUrl": ep["audioUrl"], "type": ep.get("type", "PODCAST"),
        "language": ep.get("language", "EN"), "category": ep.get("category", "TEACHING"),
        "thumbnailUrl": ep.get("thumbnailUrl", ""), "isDownloadable": True,
        "publishedAt": ep.get("publishedAt", 0), "isLive": False, "listenerCount": 0,
    })

# Add 3 live radio channels
data["content"].extend([
    {"id": "live_worship", "title": "Grace Worship 24/7", "description": "Continuous worship — modern + hymns.", "speaker": "", "durationMs": 0, "audioUrl": stream_url, "type": "LIVE_RADIO", "language": "EN", "category": "WORSHIP", "thumbnailUrl": "https://images.unsplash.com/photo-1516455590571-18256e5bb9ff?w=900&q=80", "isDownloadable": False, "publishedAt": 0, "isLive": True, "listenerCount": 1243},
    {"id": "live_teaching", "title": "Living Word Radio", "description": "Back-to-back verse-by-verse teaching.", "speaker": "", "durationMs": 0, "audioUrl": stream_url, "type": "LIVE_RADIO", "language": "EN", "category": "TEACHING", "thumbnailUrl": "https://images.unsplash.com/photo-1504052434569-70ad5836ab65?w=900&q=80", "isDownloadable": False, "publishedAt": 0, "isLive": True, "listenerCount": 856},
    {"id": "live_regional", "title": "Grace Telugu Radio", "description": "తెలుగు కీర్తనలు మరియు బోధలు. 24/7.", "speaker": "", "durationMs": 0, "audioUrl": stream_url, "type": "LIVE_RADIO", "language": "TE", "category": "REGIONAL", "thumbnailUrl": "https://images.unsplash.com/photo-1496024840928-4c417adf211d?w=900&q=80", "isDownloadable": False, "publishedAt": 0, "isLive": True, "listenerCount": 612},
])

# Live sessions
data["liveSessions"] = [
    {"id": "session_001", "title": "Live Q&A: Suffering & Sovereignty", "description": "Open mic. Ask Pastor Anil anything.", "hosts": ["Pastor Anil Kumar"], "startTime": now, "endTime": now + 7200000, "status": "LIVE", "participantCount": 327, "streamUrl": stream_url, "chatEnabled": True, "language": "EN", "category": "DEBATES", "coverImageUrl": "https://images.unsplash.com/photo-1504052434569-70ad5836ab65?w=900&q=80"},
    {"id": "session_002", "title": "Youth Debate: Faith vs Science", "description": "Genesis 1 walkthrough.", "hosts": ["Sam", "Dr. Anita", "Mark"], "startTime": now + 86400000, "endTime": now + 93600000, "status": "UPCOMING", "participantCount": 0, "streamUrl": stream_url, "chatEnabled": True, "language": "EN", "category": "DEBATES", "coverImageUrl": "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=900&q=80"},
    {"id": "session_003", "title": "Telugu Worship Night", "description": "కీర్తనలు, ప్రార్థన, సహవాసం.", "hosts": ["Pas. Raju Venkat"], "startTime": now + 172800000, "endTime": now + 180000000, "status": "UPCOMING", "participantCount": 0, "streamUrl": stream_url, "chatEnabled": True, "language": "TE", "category": "REGIONAL", "coverImageUrl": "https://images.unsplash.com/photo-1496024840928-4c417adf211d?w=900&q=80"},
]

# Prayers
data["prayers"] = [
    {"id": "pr_001", "userId": None, "displayName": None, "text": "Please pray for my mother's surgery on Friday.", "timestamp": now - 7200000, "prayedCount": 47, "isAnswered": False, "isMine": False, "userPrayedThis": False, "status": "APPROVED", "encouragements": []},
    {"id": "pr_002", "userId": "u_002", "displayName": "Daniel", "text": "Job interview tomorrow. Praying for peace and clarity.", "timestamp": now - 18000000, "prayedCount": 89, "isAnswered": False, "isMine": False, "userPrayedThis": False, "status": "APPROVED", "encouragements": []},
    {"id": "pr_003", "userId": "u_003", "displayName": "Lydia", "text": "GOD ANSWERED! My brother came to church with me!", "timestamp": now - 100800000, "prayedCount": 213, "isAnswered": True, "isMine": False, "userPrayedThis": False, "status": "APPROVED", "encouragements": [{"id":"e1","displayName":"Anonymous","text":"Rejoicing with you!","timestamp":now-72000000},{"id":"e2","displayName":"Sara","text":"Hallelujah!","timestamp":now-54000000}]},
    {"id": "pr_004", "userId": None, "displayName": None, "text": "Struggling with anxiety this week. Pray for peace.", "timestamp": now - 144000000, "prayedCount": 156, "isAnswered": False, "isMine": False, "userPrayedThis": False, "status": "APPROVED", "encouragements": []},
    {"id": "pr_005", "userId": "u_005", "displayName": "Mark", "text": "Pray for our church plant in Warangal.", "timestamp": now - 259200000, "prayedCount": 78, "isAnswered": False, "isMine": False, "userPrayedThis": False, "status": "APPROVED", "encouragements": []},
    {"id": "pr_006", "userId": "u_006", "displayName": "Auntie Mary", "text": "Healed of stage-3 cancer — 5 years clear!", "timestamp": now - 360000000, "prayedCount": 432, "isAnswered": True, "isMine": False, "userPrayedThis": False, "status": "APPROVED", "encouragements": []},
]

# Chat messages
data["chatMessages"] = [
    {"id": "m1", "sessionId": "session_001", "userId": None, "displayName": "Moderator", "text": "Welcome everyone — questions in the queue will be answered in order.", "timestamp": now - 3600000, "isModerator": True, "isHost": False, "isQuestion": False, "isMine": False},
    {"id": "m2", "sessionId": "session_001", "userId": "u_010", "displayName": "Samuel", "text": "Pastor, how do we reconcile God's sovereignty with our pain?", "timestamp": now - 3300000, "isModerator": False, "isHost": False, "isQuestion": True, "isMine": False},
    {"id": "m3", "sessionId": "session_001", "userId": "u_011", "displayName": "Sara", "text": "We're praying with you Samuel", "timestamp": now - 3240000, "isModerator": False, "isHost": False, "isQuestion": False, "isMine": False},
    {"id": "m4", "sessionId": "session_001", "userId": "u_anil", "displayName": "Pastor Anil", "text": "Great question — let's turn to Romans 8:28 first.", "timestamp": now - 3000000, "isModerator": False, "isHost": True, "isQuestion": False, "isMine": False},
    {"id": "m5", "sessionId": "session_001", "userId": "u_012", "displayName": "Raju", "text": "Glory to God for this time.", "timestamp": now - 2400000, "isModerator": False, "isHost": False, "isQuestion": False, "isMine": False},
]

# User
data["user"] = {"uid": "u_demo", "displayName": "Cornelius", "email": "cornelius@gracelink.app", "photoUrl": None, "preferredLanguage": "EN", "createdAt": now - 5184000000, "totalMinutes": 503, "completedItems": 14, "prayersOffered": 31, "streakDays": 7, "dataSaverEnabled": False, "notificationsEnabled": True}

# Build FM schedule with REAL content from episodes
worship_eps = [e for e in episodes if e['category'] == 'WORSHIP']
teaching_eps = [e for e in episodes if e['category'] == 'TEACHING']

fm_schedule = []
days = ["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"]
for day in days:
    for hour in range(0, 24, 2):
        slot = f"{hour:02d}:00-{hour+2:02d}:00"
        if hour < 6:
            preacher = "Midnight Praise" if hour < 4 else "Dawn Devotional"
            desc = "Worship and prayer" if hour < 4 else "Morning devotions"
            cat = "WORSHIP" if hour < 4 else "TEACHING"
        elif hour < 12:
            if day in ("SAT", "SUN"):
                preacher = "Special Guest Sermon" if hour == 6 else "Timothy Keller"
            else:
                preacher = "Pastor Anil Kumar" if hour == 6 else ("Timothy Keller" if hour == 8 else "Hillsong Worship")
            desc = "Morning sermon" if hour < 8 else ("Teaching" if hour < 10 else "Worship music")
            cat = "TEACHING" if hour < 10 else "WORSHIP"
        elif hour < 18:
            preacher = "John MacArthur" if hour == 12 else ("Alistair Begg" if hour == 14 else "Bethel Music")
            desc = "Expository preaching" if hour < 14 else ("Bible teaching" if hour < 16 else "Worship session")
            cat = "TEACHING" if hour < 16 else "WORSHIP"
        else:
            if hour == 18:
                preacher = "Paul Washer"; desc = "Gospel preaching"; cat = "TEACHING"
            elif hour == 20:
                preacher = "Pas. Raju Venkat" if day not in ("SAT","SUN") else "Testimony Time"
                desc = "Telugu sermons" if day not in ("SAT","SUN") else "God's faithfulness"
                cat = "REGIONAL" if day not in ("SAT","SUN") else "TESTIMONY"
            else:
                preacher = "Night Worship"; desc = "End your day with worship"; cat = "WORSHIP"
        fm_schedule.append({"slot": slot, "preacher": preacher, "description": desc, "category": cat, "day": day})

out_path = '/home/z/my-project/app/src/main/assets/gracelink_data.json'
os.makedirs(os.path.dirname(out_path), exist_ok=True)
with open(out_path, 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

fm_path = '/home/z/my-project/app/src/main/assets/fm_schedule.json'
with open(fm_path, 'w', encoding='utf-8') as f:
    json.dump(fm_schedule, f, indent=2, ensure_ascii=False)

print(f"Content items: {len(data['content'])} ({len(episodes)} real episodes + 3 radio)")
print(f"FM schedule: {len(fm_schedule)} slots")
print(f"Categories: WORSHIP={len([e for e in episodes if e['category']=='WORSHIP'])}, TEACHING={len([e for e in episodes if e['category']=='TEACHING'])}")
