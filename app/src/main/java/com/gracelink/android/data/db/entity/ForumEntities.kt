package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val answerCount: Int = 0,
)

@Entity(tableName = "answers")
data class AnswerEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val createdAt: Long,
    // If this answer is itself a reply to another answer (not just the
    // original question), both fields are set so the UI can clearly show
    // "so-and-so replying to so-and-so" -- the whole point of asking for
    // this feature was making sure that attribution never gets lost.
    val replyToAnswerId: String? = null,
    val replyToAuthorName: String? = null,
)
