package com.gracelink.android.data.db.converter

import androidx.room.TypeConverter
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.ContentType
import com.gracelink.android.data.db.entity.LiveSessionStatus
import com.gracelink.android.data.db.entity.PrayerStatus

/**
 * Type converters so Room stores enums as their string names instead of
 * ordinals. This makes SQL queries like `WHERE type = 'LIVE_RADIO'` work
 * correctly and is more robust across schema changes.
 */
class EnumConverters {

    @TypeConverter fun contentTypeToString(t: ContentType?): String? = t?.name
    @TypeConverter fun stringToContentType(s: String?): ContentType? = s?.let { ContentType.valueOf(it) }

    @TypeConverter fun contentLanguageToString(l: ContentLanguage?): String? = l?.name
    @TypeConverter fun stringToContentLanguage(s: String?): ContentLanguage? = s?.let { ContentLanguage.valueOf(it) }

    @TypeConverter fun contentCategoryToString(c: ContentCategory?): String? = c?.name
    @TypeConverter fun stringToContentCategory(s: String?): ContentCategory? = s?.let { ContentCategory.valueOf(it) }

    @TypeConverter fun liveSessionStatusToString(s: LiveSessionStatus?): String? = s?.name
    @TypeConverter fun stringToLiveSessionStatus(s: String?): LiveSessionStatus? = s?.let { LiveSessionStatus.valueOf(it) }

    @TypeConverter fun prayerStatusToString(s: PrayerStatus?): String? = s?.name
    @TypeConverter fun stringToPrayerStatus(s: String?): PrayerStatus? = s?.let { PrayerStatus.valueOf(it) }
}
