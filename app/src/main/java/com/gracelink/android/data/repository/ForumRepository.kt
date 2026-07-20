package com.gracelink.android.data.repository

import com.gracelink.android.data.db.dao.ForumDao
import com.gracelink.android.data.db.entity.AnswerEntity
import com.gracelink.android.data.db.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForumRepository @Inject constructor(
    private val dao: ForumDao,
) {
    fun allQuestions(): Flow<List<QuestionEntity>> = dao.allQuestions()
    fun questionById(id: String): Flow<QuestionEntity?> = dao.questionById(id)
    fun answersFor(questionId: String): Flow<List<AnswerEntity>> = dao.answersFor(questionId)

    suspend fun askQuestion(authorId: String, authorName: String, title: String, body: String): String {
        val id = "q_${System.currentTimeMillis()}"
        dao.insertQuestion(
            QuestionEntity(
                id = id, authorId = authorId, authorName = authorName,
                title = title, body = body, createdAt = System.currentTimeMillis(),
            )
        )
        return id
    }

    /**
     * [replyToAnswerId]/[replyToAuthorName] are set when this answer is a
     * reply to another answer rather than a fresh answer to the question
     * itself -- keeps it always clear who's responding to whom.
     */
    suspend fun postAnswer(
        questionId: String,
        authorId: String,
        authorName: String,
        text: String,
        replyToAnswerId: String? = null,
        replyToAuthorName: String? = null,
    ) {
        dao.insertAnswer(
            AnswerEntity(
                id = "a_${System.currentTimeMillis()}",
                questionId = questionId,
                authorId = authorId,
                authorName = authorName,
                text = text,
                createdAt = System.currentTimeMillis(),
                replyToAnswerId = replyToAnswerId,
                replyToAuthorName = replyToAuthorName,
            )
        )
        dao.incrementAnswerCount(questionId)
    }
}
