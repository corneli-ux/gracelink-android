package com.gracelink.android.feature.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.dao.*
import com.gracelink.android.data.db.entity.*
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticlesState(
    val articles: List<ArticleEntity> = emptyList(),
    val showWrite: Boolean = false,
    val myName: String = "You",
    val myId: String = "u_demo",
)

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val articleDao: ArticleDao,
    private val commentDao: ArticleCommentDao,
    private val likeDao: ArticleLikeDao,
    userRepo: UserRepository,
) : ViewModel() {

    private val showWrite = MutableStateFlow(false)

    val state: StateFlow<ArticlesState> = combine(articleDao.all(), showWrite, userRepo.current()) { articles, write, user ->
        ArticlesState(articles, write, user?.displayName ?: "You", user?.uid ?: "u_demo")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticlesState())

    fun showWriteDialog(show: Boolean) { showWrite.value = show }

    fun writeArticle(title: String, content: String) = viewModelScope.launch {
        val s = state.value
        val article = ArticleEntity(
            id = "art_${System.currentTimeMillis()}",
            authorId = s.myId,
            authorName = s.myName,
            authorType = AccountType.PERSONAL,
            churchId = null,
            title = title,
            content = content,
            publishedAt = System.currentTimeMillis(),
            likeCount = 0,
            commentCount = 0,
            tags = "",
        )
        articleDao.insert(article)
        showWrite.value = false
    }

    fun toggleLike(articleId: String) = viewModelScope.launch {
        val s = state.value
        val isLiked = likeDao.isLiked(articleId, s.myId).first()
        if (isLiked) {
            likeDao.remove(articleId, s.myId)
            articleDao.decrementLikes(articleId)
        } else {
            likeDao.insert(ArticleLikeEntity("$articleId_${s.myId}", articleId, s.myId, System.currentTimeMillis()))
            articleDao.incrementLikes(articleId)
        }
    }

    fun addComment(articleId: String, text: String) = viewModelScope.launch {
        val s = state.value
        commentDao.insert(ArticleCommentEntity("c_${System.currentTimeMillis()}", articleId, s.myId, s.myName, text, System.currentTimeMillis()))
        articleDao.incrementComments(articleId)
    }
}

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val articleDao: ArticleDao,
    private val commentDao: ArticleCommentDao,
    private val likeDao: ArticleLikeDao,
    userRepo: UserRepository,
) : ViewModel() {

    val article = MutableStateFlow<ArticleEntity?>(null)
    val comments: StateFlow<List<ArticleCommentEntity>> = article.flatMapLatest { a ->
        if (a != null) commentDao.forArticle(a.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val myId = MutableStateFlow("u_demo")

    init {
        viewModelScope.launch {
            userRepo.current().collect { u -> myId.value = u?.uid ?: "u_demo" }
        }
    }

    fun load(articleId: String) = viewModelScope.launch {
        article.value = articleDao.getById(articleId)
    }

    fun toggleLike() = viewModelScope.launch {
        val a = article.value ?: return@launch
        val isLiked = likeDao.isLiked(a.id, myId.value).first()
        if (isLiked) { likeDao.remove(a.id, myId.value); articleDao.decrementLikes(a.id) }
        else { likeDao.insert(ArticleLikeEntity("${a.id}_${myId.value}", a.id, myId.value, System.currentTimeMillis())); articleDao.incrementLikes(a.id) }
        article.value = articleDao.getById(a.id)
    }

    fun addComment(text: String) = viewModelScope.launch {
        val a = article.value ?: return@launch
        commentDao.insert(ArticleCommentEntity("c_${System.currentTimeMillis()}", a.id, myId.value, "You", text, System.currentTimeMillis()))
        articleDao.incrementComments(a.id)
        article.value = articleDao.getById(a.id)
    }
}
