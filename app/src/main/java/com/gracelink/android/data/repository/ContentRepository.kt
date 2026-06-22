package com.gracelink.android.data.repository

import com.gracelink.android.data.mock.MockData
import com.gracelink.android.data.model.ContentCategory
import com.gracelink.android.data.model.ContentItem
import com.gracelink.android.data.model.ContentLanguage
import com.gracelink.android.data.model.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Content repository — abstracts where content comes from.
 *
 * MVP: backed by [MockData]. Phase 2: swap [fetchHome], [fetchLibrary] etc. to
 * call Firestore / Retrofit — the ViewModels don't need to change.
 */
@Singleton
class ContentRepository @Inject constructor() {

    private val _library = MutableStateFlow(MockData.onDemandLibrary)
    val library: StateFlow<List<ContentItem>> = _library.asStateFlow()

    private val _liveRadio = MutableStateFlow(MockData.liveRadioChannels)
    val liveRadio: StateFlow<List<ContentItem>> = _liveRadio.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _downloads = MutableStateFlow<Set<String>>(emptySet())
    val downloads: StateFlow<Set<String>> = _downloads.asStateFlow()

    private val _continueListening = MutableStateFlow(MockData.continueListening)
    val continueListening: StateFlow<List<Pair<ContentItem, Long>>> = _continueListening.asStateFlow()

    /** Simulate a network round-trip so the UI shows loading state. */
    suspend fun fetchHome(): HomeData = withContext(Dispatchers.IO) {
        delay(400)
        HomeData(
            liveRadio = _liveRadio.value,
            continueListening = _continueListening.value,
            recommended = _library.value.shuffled().take(6),
        )
    }

    suspend fun searchLibrary(
        query: String = "",
        category: ContentCategory? = null,
        language: ContentLanguage? = null,
        type: ContentType? = null,
    ): List<ContentItem> = withContext(Dispatchers.IO) {
        delay(250)
        _library.value.filter { item ->
            (query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                (item.speaker?.contains(query, ignoreCase = true) ?: false)) &&
            (category == null || item.category == category) &&
            (language == null || item.language == language) &&
            (type == null || item.type == type)
        }
    }

    fun toggleFavorite(id: String) {
        _favorites.value = _favorites.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
    }

    fun toggleDownload(id: String) {
        _downloads.value = _downloads.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
    }

    fun getById(id: String): ContentItem? =
        (_library.value + _liveRadio.value).firstOrNull { it.id == id }
}

data class HomeData(
    val liveRadio: List<ContentItem>,
    val continueListening: List<Pair<ContentItem, Long>>,
    val recommended: List<ContentItem>,
)
