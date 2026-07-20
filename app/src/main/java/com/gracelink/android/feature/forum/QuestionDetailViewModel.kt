package com.gracelink.android.feature.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.AnswerEntity
import com.gracelink.android.data.db.entity.QuestionEntity
import com.gracelink.android.data.repository.ForumRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class QuestionDetailState(
    val question: QuestionEntity? = null,
    val answers: List<AnswerEntity> = emptyList(),
    val myName: String = "",
    val myUid: String = "",
    val isGuest: Boolean = true,
)

@HiltViewModel
class QuestionDetailViewModel @Inject constructor(
    private val repo: ForumRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val questionId = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<QuestionDetailState> = combine(
        questionId.flatMapLatest { id -> if (id.isBlank()) flowOf(null) else repo.questionById(id) },
        questionId.flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.answersFor(id) },
        userRepo.current(),
    ) { question, answers, user ->
        QuestionDetailState(
            question = question,
            answers = answers,
            myName = user?.displayName ?: "",
            myUid = user?.uid ?: "",
            isGuest = user == null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuestionDetailState())

    fun load(id: String) { questionId.value = id }

    /**
     * [replyTo] is the specific answer being responded to, if any -- when
     * set, the reply is clearly attributed to that answer's author rather
     * than just appearing as a generic new answer to the question.
     */
    fun postAnswer(text: String, replyTo: AnswerEntity?) = viewModelScope.launch {
        val s = state.value
        val qid = s.question?.id ?: return@launch
        if (s.myUid.isBlank() || text.isBlank()) return@launch
        repo.postAnswer(
            questionId = qid,
            authorId = s.myUid,
            authorName = s.myName,
            text = text,
            replyToAnswerId = replyTo?.id,
            replyToAuthorName = replyTo?.authorName,
        )
    }
}
