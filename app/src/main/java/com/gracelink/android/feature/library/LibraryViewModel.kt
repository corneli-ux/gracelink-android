package com.gracelink.android.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.ContentType
import com.gracelink.android.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LibraryState(
    val query: String = "",
    val category: ContentCategory? = null,
    val language: ContentLanguage? = null,
    val type: ContentType? = null,
    val items: List<ContentEntity> = emptyList(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: ContentRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<ContentCategory?>(null)
    private val language = MutableStateFlow<ContentLanguage?>(null)
    private val type = MutableStateFlow<ContentType?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val itemsFlow = combine(query, category, language, type) { q, c, l, t ->
        FilterState(q, c, l, t)
    }.flatMapLatest { f -> repo.search(f.query, f.category, f.language, f.type) }

    val state: StateFlow<LibraryState> = combine(query, category, language, type, itemsFlow) { q, c, l, t, items ->
        LibraryState(q, c, l, t, items)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryState())

    private data class FilterState(val query: String, val category: ContentCategory?, val language: ContentLanguage?, val type: ContentType?)

    fun setQuery(q: String) { query.value = q }
    fun setCategory(c: ContentCategory?) { category.value = c }
    fun setLanguage(l: ContentLanguage?) { language.value = l }
    fun setType(t: ContentType?) { type.value = t }
}
