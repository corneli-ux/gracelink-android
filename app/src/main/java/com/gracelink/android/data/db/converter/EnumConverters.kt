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

    @TypeConverter fun memberStatusToString(s: MemberStatus?): String? = s?.name
    @TypeConverter fun stringToMemberStatus(s: String?): MemberStatus? = s?.let { MemberStatus.valueOf(it) }

    // Church administration
    @TypeConverter fun churchRoleToString(r: ChurchRole?): String? = r?.name
    @TypeConverter fun stringToChurchRole(s: String?): ChurchRole? = s?.let { ChurchRole.valueOf(it) }

    @TypeConverter fun groupTypeToString(t: GroupType?): String? = t?.name
    @TypeConverter fun stringToGroupType(s: String?): GroupType? = s?.let { GroupType.valueOf(it) }

    @TypeConverter fun announcementPriorityToString(p: AnnouncementPriority?): String? = p?.name
    @TypeConverter fun stringToAnnouncementPriority(s: String?): AnnouncementPriority? = s?.let { AnnouncementPriority.valueOf(it) }

    @TypeConverter fun rsvpStatusToString(s: RsvpStatus?): String? = s?.name
    @TypeConverter fun stringToRsvpStatus(s: String?): RsvpStatus? = s?.let { RsvpStatus.valueOf(it) }

    @TypeConverter fun messageTypeToString(t: MessageType?): String? = t?.name
    @TypeConverter fun stringToMessageType(s: String?): MessageType? = s?.let { MessageType.valueOf(it) }
}
