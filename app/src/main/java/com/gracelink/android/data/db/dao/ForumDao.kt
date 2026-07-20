package com.gracelink.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracelink.android.data.db.entity.AnswerEntity
import com.gracelink.android.data.db.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {
    @Query("SELECT * FROM questions ORDER BY createdAt DESC")
    fun allQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    fun questionById(id: String): Flow<QuestionEntity?>

    @Query("SELECT * FROM questions WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun questionsByAuthor(authorId: String): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Query("SELECT * FROM answers WHERE questionId = :questionId ORDER BY createdAt ASC")
    fun answersFor(questionId: String): Flow<List<AnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity)

    @Query("UPDATE questions SET answerCount = answerCount + 1 WHERE id = :questionId")
    suspend fun incrementAnswerCount(questionId: String)
}
