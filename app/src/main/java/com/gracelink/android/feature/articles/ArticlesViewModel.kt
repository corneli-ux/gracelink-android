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
    val isGuest: Boolean = true,
)

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val articleDao: ArticleDao,
    private val commentDao: ArticleCommentDao,
    private val likeDao: ArticleLikeDao,
    private val churchDao: ChurchDao,
    private val userRepo: UserRepository,
) : ViewModel() {

    private val showWrite = MutableStateFlow(false)

    val state: StateFlow<ArticlesState> = combine(articleDao.all(), showWrite, userRepo.current()) { articles, write, user ->
        ArticlesState(articles, write, user?.displayName ?: "You", user?.uid ?: "u_demo", isGuest = user == null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticlesState())

    fun showWriteDialog(show: Boolean) { showWrite.value = show }

    /**
     * Resolves the real account type and church via a direct suspend query
     * instead of hardcoding PERSONAL/null. That hardcoding meant an article
     * written by a Church or Pastor account here was silently misattributed
     * as a plain personal post with no church link -- it would never show
     * up on that church's own public profile, since that page queries
     * articles by churchId.
     */
    fun writeArticle(title: String, content: String) = viewModelScope.launch {
        val user = userRepo.currentOnce() ?: return@launch
        val church = if (user.accountType == AccountType.CHURCH) churchDao.byOwnerOnce(user.uid) else null
        val article = ArticleEntity(
            id = "art_${System.currentTimeMillis()}",
            authorId = user.uid,
            authorName = church?.name ?: user.displayName,
            authorType = user.accountType,
            churchId = church?.id,
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
            likeDao.insert(ArticleLikeEntity(articleId + "_" + s.myId, articleId, s.myId, System.currentTimeMillis()))
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

    // Comments are keyed off a stable articleId, not off `article` itself.
    // Previously `comments` was derived via article.flatMapLatest { ... } --
    // but addComment() and toggleLike() both reassign article.value (to a
    // freshly-fetched ArticleEntity, to refresh the like/comment counts
    // shown on screen), and flatMapLatest restarts its downstream flow on
    // every new upstream value. Every comment submitted was cancelling and
    // re-subscribing the comments query, which visibly emptied and
    // reloaded the list -- reading exactly like "fluctuating" or "the
    // comment didn't go through" even though it actually had.
    private val articleId = MutableStateFlow<String?>(null)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val comments: StateFlow<List<ArticleCommentEntity>> = articleId.flatMapLatest { id ->
        if (id != null) commentDao.forArticle(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val myId = MutableStateFlow("u_demo")
    private val myName = MutableStateFlow("You")

    init {
        viewModelScope.launch {
            userRepo.current().collect { u ->
                myId.value = u?.uid ?: "u_demo"
                myName.value = u?.displayName?.takeIf { it.isNotBlank() } ?: "You"
            }
        }
    }

    fun load(articleId: String) = viewModelScope.launch {
        this@ArticleDetailViewModel.articleId.value = articleId
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
        commentDao.insert(ArticleCommentEntity("c_${System.currentTimeMillis()}", a.id, myId.value, myName.value, text, System.currentTimeMillis()))
        articleDao.incrementComments(a.id)
        article.value = articleDao.getById(a.id)
    }
}
