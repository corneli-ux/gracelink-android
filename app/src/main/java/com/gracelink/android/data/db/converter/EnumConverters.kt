package com.gracelink.android.data.db.converter

import androidx.room.TypeConverter
import com.gracelink.android.data.db.entity.*

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

    @TypeConverter fun accountTypeToString(t: AccountType?): String? = t?.name
    @TypeConverter fun stringToAccountType(s: String?): AccountType? = s?.let { AccountType.valueOf(it) }

    @TypeConverter fun beliefSystemToString(b: BeliefSystem?): String? = b?.name
    @TypeConverter fun stringToBeliefSystem(s: String?): BeliefSystem? = s?.let { BeliefSystem.valueOf(it) }

    @TypeConverter fun verificationStatusToString(v: VerificationStatus?): String? = v?.name
    @TypeConverter fun stringToVerificationStatus(s: String?): VerificationStatus? = s?.let { VerificationStatus.valueOf(it) }
}
