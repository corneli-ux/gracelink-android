package com.gracelink.android.data.model

import kotlinx.serialization.Serializable

/**
 * A single chat message in a live session's text participation panel.
 * Spec §3 "Text Participation": text chat + question submission (moderated queue).
 */
@Serializable
data class ChatMessage(
    val id: String,
    val sessionId: String,
    val userId: String?,
    val displayName: String,
    val text: String,
    val timestamp: Long,
    val isModerator: Boolean = false,
    val isHost: Boolean = false,
    val isQuestion: Boolean = false,
    val isMine: Boolean = false,
)
