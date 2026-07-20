package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.*
import com.gracelink.android.data.db.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChurchAdminRepository @Inject constructor(
    private val announcementDao: AnnouncementDao,
    private val groupDao: ChurchGroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val rsvpDao: EventRsvpDao,
    private val leadershipDao: LeadershipDao,
    private val ministryDao: MinistryDao,
    private val groupMessageDao: GroupMessageDao,
    private val dmDao: DirectMessageDao,
    private val serviceTimeDao: ServiceTimeDao,
    private val adminNoteDao: AdminNoteDao,
    private val moderationDao: ModerationDao,
    private val memberDao: ChurchMemberDao,
) {
    // ── Announcements ──────────────────────────────────────────
    fun announcements(churchId: String) = announcementDao.activeForChurch(churchId)
    suspend fun createAnnouncement(
        churchId: String, authorId: String, authorName: String,
        title: String, body: String, priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
        targetRoles: List<ChurchRole>? = null, expiresAt: Long? = null
    ) {
        announcementDao.insert(
            AnnouncementEntity(
                id = UUID.randomUUID().toString(),
                churchId = churchId,
                authorId = authorId,
                authorName = authorName,
                title = title,
                body = body,
                priority = priority,
                createdAt = System.currentTimeMillis(),
                expiresAt = expiresAt,
                targetRolesJson = targetRoles?.joinToString(",") { it.name },
                isPinned = false
            )
        )
    }
    suspend fun deleteAnnouncement(id: String) = announcementDao.delete(id)
    suspend fun pinAnnouncement(id: String, pinned: Boolean) = announcementDao.setPinned(id, pinned)

    // ── Groups ─────────────────────────────────────────────────
    fun groups(churchId: String) = groupDao.forChurch(churchId)
    suspend fun getGroup(id: String) = groupDao.getById(id)
    suspend fun createGroup(
        churchId: String, name: String, description: String,
        type: GroupType, leaderUserId: String?, leaderName: String?, isPrivate: Boolean
    ): String {
        val id = UUID.randomUUID().toString()
        groupDao.insert(
            ChurchGroupEntity(
                id = id, churchId = churchId, name = name, description = description,
                type = type, leaderUserId = leaderUserId, leaderName = leaderName,
                memberCount = 0, createdAt = System.currentTimeMillis(), isPrivate = isPrivate
            )
        )
        return id
    }
    fun groupMembers(groupId: String) = groupMemberDao.forGroup(groupId)
    suspend fun addToGroup(groupId: String, churchId: String, userId: String, displayName: String, role: ChurchRole = ChurchRole.MEMBER) {
        groupMemberDao.insert(
            GroupMemberEntity(
                id = "${groupId}_$userId", groupId = groupId, churchId = churchId,
                userId = userId, displayName = displayName, role = role,
                joinedAt = System.currentTimeMillis()
            )
        )
        groupDao.incrementMemberCount(groupId)
    }
    suspend fun removeFromGroup(groupId: String, userId: String) {
        groupMemberDao.remove(groupId, userId)
        groupDao.decrementMemberCount(groupId)
    }

    // ── Group Chat ─────────────────────────────────────────────
    fun groupMessages(groupId: String) = groupMessageDao.forGroup(groupId)
    suspend fun sendGroupMessage(groupId: String, churchId: String, senderId: String, senderName: String, text: String) {
        groupMessageDao.insert(
            GroupMessageEntity(
                id = UUID.randomUUID().toString(), groupId = groupId, churchId = churchId,
                senderId = senderId, senderName = senderName, text = text,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    // ── Direct Messages ────────────────────────────────────────
    fun conversation(conversationId: String) = dmDao.forConversation(conversationId)
    suspend fun sendDirectMessage(churchId: String, senderId: String, senderName: String, receiverId: String, text: String) {
        val convId = listOf(senderId, receiverId).sorted().joinToString("_")
        dmDao.insert(
            DirectMessageEntity(
                id = UUID.randomUUID().toString(), churchId = churchId,
                conversationId = convId, senderId = senderId, senderName = senderName,
                receiverId = receiverId, text = text, createdAt = System.currentTimeMillis()
            )
        )
    }

    // ── Event RSVP ─────────────────────────────────────────────
    fun rsvps(eventId: String) = rsvpDao.forEvent(eventId)
    suspend fun setRsvp(eventId: String, churchId: String, userId: String, displayName: String, status: RsvpStatus, note: String? = null) {
        rsvpDao.upsert(
            EventRsvpEntity(
                id = "${eventId}_$userId", eventId = eventId, churchId = churchId,
                userId = userId, displayName = displayName, status = status,
                respondedAt = System.currentTimeMillis(), note = note
            )
        )
    }

    // ── Leadership & Ministries ────────────────────────────────
    fun publicLeadership(churchId: String) = leadershipDao.publicForChurch(churchId)
    fun allLeadership(churchId: String) = leadershipDao.allForChurch(churchId)
    suspend fun addLeader(churchId: String, userId: String, displayName: String, title: String, bio: String? = null, photoUrl: String? = null, sortOrder: Int = 0) {
        leadershipDao.insert(
            LeadershipMemberEntity(
                id = "${churchId}_$userId", churchId = churchId, userId = userId,
                displayName = displayName, title = title, bio = bio, photoUrl = photoUrl, sortOrder = sortOrder
            )
        )
    }
    fun ministries(churchId: String) = ministryDao.forChurch(churchId)
    suspend fun createMinistry(churchId: String, name: String, description: String, leaderUserId: String? = null, leaderName: String? = null, meetingInfo: String? = null) {
        ministryDao.insert(
            MinistryEntity(
                id = UUID.randomUUID().toString(), churchId = churchId, name = name,
                description = description, leaderUserId = leaderUserId, leaderName = leaderName,
                meetingInfo = meetingInfo
            )
        )
    }

    // ── Service Times ──────────────────────────────────────────
    fun serviceTimes(churchId: String) = serviceTimeDao.forChurch(churchId)
    suspend fun addServiceTime(churchId: String, dayOfWeek: Int, time: String, name: String, location: String? = null, isOnline: Boolean = false) {
        serviceTimeDao.insert(
            ServiceTimeEntity(
                id = UUID.randomUUID().toString(), churchId = churchId,
                dayOfWeek = dayOfWeek, time = time, name = name, location = location, isOnline = isOnline
            )
        )
    }

    // ── Admin Notes & Moderation ───────────────────────────────
    fun adminNotes(churchId: String, memberUserId: String) = adminNoteDao.forMember(churchId, memberUserId)
    suspend fun addAdminNote(churchId: String, memberUserId: String, authorId: String, authorName: String, note: String) {
        adminNoteDao.insert(
            AdminNoteEntity(
                id = UUID.randomUUID().toString(), churchId = churchId, memberUserId = memberUserId,
                authorId = authorId, authorName = authorName, note = note, createdAt = System.currentTimeMillis()
            )
        )
    }
    fun moderationLog(churchId: String) = moderationDao.recent(churchId)
    suspend fun logModeration(churchId: String, actorId: String, actorName: String, targetUserId: String?, targetContentId: String?, action: String, reason: String) {
        moderationDao.insert(
            ModerationActionEntity(
                id = UUID.randomUUID().toString(), churchId = churchId, actorId = actorId,
                actorName = actorName, targetUserId = targetUserId, targetContentId = targetContentId,
                action = action, reason = reason, createdAt = System.currentTimeMillis()
            )
        )
    }

    // ── Member role management ─────────────────────────────────
    suspend fun updateMemberRole(memberId: String, role: ChurchRole) {
        memberDao.updateRole(memberId, role)
    }

    suspend fun updateAdminNotes(memberId: String, notes: String?) {
        memberDao.updateAdminNotes(memberId, notes)
    }

    suspend fun updateMemberContactInfo(memberId: String, phone: String?, email: String?) {
        memberDao.updateContactInfo(memberId, phone, email)
    }

    suspend fun removeMember(memberId: String) {
        memberDao.removeMember(memberId)
    }
}
