package com.gracelink.android.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.model.ContentCategory
import com.gracelink.android.data.model.ContentItem
import com.gracelink.android.data.model.ContentLanguage
import com.gracelink.android.data.model.ContentType
import com.gracelink.android.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val items: List<ContentItem> = emptyList(),
    val activeCategory: ContentCategory? = null,
    val activeLanguage: ContentLanguage? = null,
    val activeType: ContentType? = null,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ContentRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _activeCategory = MutableStateFlow<ContentCategory?>(null)
    private val _activeLanguage = MutableStateFlow<ContentLanguage?>(null)
    private val _activeType = MutableStateFlow<ContentType?>(null)
    private val _items = MutableStateFlow<List<ContentItem>>(emptyList())
    private val _isLoading = MutableStateFlow(false)

    // 5-arg combine is the maximum typed overload; wrap query+category into one
    // pair to stay under the limit.
    private data class Filters(
        val query: String,
        val category: ContentCategory?,
        val language: ContentLanguage?,
        val type: ContentType?,
    )

    val state: StateFlow<LibraryUiState> = combine(
        combine(_query, _activeCategory, _activeLanguage, _activeType) { q, c, l, t ->
            Filters(q, c, l, t)
        },
        _items,
        _isLoading,
    ) { filters, items, loading ->
        LibraryUiState(
            isLoading = loading,
            query = filters.query,
            items = items,
            activeCategory = filters.category,
            activeLanguage = filters.language,
            activeType = filters.type,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    init { refresh() }

    fun setQuery(q: String) { _query.value = q; refresh() }
    fun setCategory(c: ContentCategory?) { _activeCategory.value = c; refresh() }
    fun setLanguage(l: ContentLanguage?) { _activeLanguage.value = l; refresh() }
    fun setType(t: ContentType?) { _activeType.value = t; refresh() }

    private fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _items.value = repository.searchLibrary(
                query = _query.value,
                category = _activeCategory.value,
                language = _activeLanguage.value,
                type = _activeType.value,
            )
            _isLoading.value = false
        }
    }
}
