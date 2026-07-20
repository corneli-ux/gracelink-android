package com.gracelink.android.data.db.dao

import androidx.room.*
import com.gracelink.android.data.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE churchId = :churchId ORDER BY isPinned DESC, createdAt DESC")
    fun forChurch(churchId: String): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE churchId = :churchId AND (expiresAt IS NULL OR expiresAt > :now) ORDER BY isPinned DESC, createdAt DESC")
    fun activeForChurch(churchId: String, now: Long = System.currentTimeMillis()): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE announcements SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)
}

@Dao
interface ChurchGroupDao {
    @Query("SELECT * FROM church_groups WHERE churchId = :churchId ORDER BY name ASC")
    fun forChurch(churchId: String): Flow<List<ChurchGroupEntity>>

    @Query("SELECT * FROM church_groups WHERE id = :id")
    suspend fun getById(id: String): ChurchGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: ChurchGroupEntity)

    @Update
    suspend fun update(group: ChurchGroupEntity)

    @Query("UPDATE church_groups SET memberCount = memberCount + 1 WHERE id = :groupId")
    suspend fun incrementMemberCount(groupId: String)

    @Query("UPDATE church_groups SET memberCount = MAX(0, memberCount - 1) WHERE id = :groupId")
    suspend fun decrementMemberCount(groupId: String)

    @Query("DELETE FROM church_groups WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface GroupMemberDao {
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND isActive = 1 ORDER BY joinedAt ASC")
    fun forGroup(groupId: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM group_members WHERE userId = :userId AND isActive = 1")
    fun forUser(userId: String): Flow<List<GroupMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: GroupMemberEntity)

    @Query("UPDATE group_members SET isActive = 0 WHERE groupId = :groupId AND userId = :userId")
    suspend fun remove(groupId: String, userId: String)

    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId AND isActive = 1")
    suspend fun countActive(groupId: String): Int
}

@Dao
interface EventRsvpDao {
    @Query("SELECT * FROM event_rsvps WHERE eventId = :eventId ORDER BY respondedAt DESC")
    fun forEvent(eventId: String): Flow<List<EventRsvpEntity>>

    @Query("SELECT * FROM event_rsvps WHERE eventId = :eventId AND userId = :userId LIMIT 1")
    suspend fun get(eventId: String, userId: String): EventRsvpEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rsvp: EventRsvpEntity)

    @Query("SELECT COUNT(*) FROM event_rsvps WHERE eventId = :eventId AND status = 'GOING'")
    suspend fun countGoing(eventId: String): Int
}

@Dao
interface LeadershipDao {
    @Query("SELECT * FROM leadership_team WHERE churchId = :churchId AND isPublic = 1 ORDER BY sortOrder ASC")
    fun publicForChurch(churchId: String): Flow<List<LeadershipMemberEntity>>

    @Query("SELECT * FROM leadership_team WHERE churchId = :churchId ORDER BY sortOrder ASC")
    fun allForChurch(churchId: String): Flow<List<LeadershipMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: LeadershipMemberEntity)

    @Query("DELETE FROM leadership_team WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MinistryDao {
    @Query("SELECT * FROM ministries WHERE churchId = :churchId AND isActive = 1 ORDER BY sortOrder ASC")
    fun forChurch(churchId: String): Flow<List<MinistryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ministry: MinistryEntity)

    @Update
    suspend fun update(ministry: MinistryEntity)

    @Query("UPDATE ministries SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)
}

@Dao
interface GroupMessageDao {
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId AND isDeleted = 0 ORDER BY createdAt ASC")
    fun forGroup(groupId: String): Flow<List<GroupMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: GroupMessageEntity)

    @Query("UPDATE group_messages SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)
}

@Dao
interface DirectMessageDao {
    @Query("SELECT * FROM direct_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun forConversation(conversationId: String): Flow<List<DirectMessageEntity>>

    @Query("SELECT * FROM direct_messages WHERE churchId = :churchId AND (senderId = :uid OR receiverId = :uid) ORDER BY createdAt DESC")
    fun recentForUser(churchId: String, uid: String): Flow<List<DirectMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: DirectMessageEntity)

    @Query("UPDATE direct_messages SET isRead = 1 WHERE conversationId = :conversationId AND receiverId = :uid")
    suspend fun markRead(conversationId: String, uid: String)
}

@Dao
interface ServiceTimeDao {
    @Query("SELECT * FROM service_times WHERE churchId = :churchId ORDER BY dayOfWeek ASC, sortOrder ASC")
    fun forChurch(churchId: String): Flow<List<ServiceTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ServiceTimeEntity)

    @Query("DELETE FROM service_times WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface AdminNoteDao {
    @Query("SELECT * FROM admin_notes WHERE churchId = :churchId AND memberUserId = :memberUserId ORDER BY createdAt DESC")
    fun forMember(churchId: String, memberUserId: String): Flow<List<AdminNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: AdminNoteEntity)
}

@Dao
interface ModerationDao {
    @Query("SELECT * FROM moderation_actions WHERE churchId = :churchId ORDER BY createdAt DESC LIMIT 100")
    fun recent(churchId: String): Flow<List<ModerationActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: ModerationActionEntity)
}
