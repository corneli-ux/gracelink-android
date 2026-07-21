package com.gracelink.android.feature.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.QuestionEntity
import com.gracelink.android.data.repository.ForumRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForumState(
    val questions: List<QuestionEntity> = emptyList(),
    val myName: String = "",
    val myUid: String = "",
    val isGuest: Boolean = true,
)

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val repo: ForumRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    val state: StateFlow<ForumState> = combine(repo.allQuestions(), userRepo.current()) { questions, user ->
        ForumState(
            questions = questions,
            myName = user?.displayName ?: "",
            myUid = user?.uid ?: "",
            isGuest = user == null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ForumState())

    /** Resolves the asker directly via a one-shot suspend query rather than
     * reading state.value, which stays at its default (blank uid) forever
     * on a create-only screen that never collects [state]. */
    fun askQuestion(title: String, body: String, onDone: (String) -> Unit) = viewModelScope.launch {
        val user = userRepo.currentOnce() ?: return@launch
        if (title.isBlank()) return@launch
        val id = repo.askQuestion(user.uid, user.displayName, title, body)
        onDone(id)
    }
}
