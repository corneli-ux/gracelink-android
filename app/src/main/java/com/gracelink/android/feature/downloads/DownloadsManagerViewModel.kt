package com.gracelink.android.feature.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.DownloadEntity
import com.gracelink.android.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsManagerViewModel @Inject constructor(
    private val repo: DownloadRepository,
) : ViewModel() {

    val downloads: StateFlow<List<DownloadEntity>> = repo.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remove(contentId: String) = viewModelScope.launch {
        repo.remove(contentId)
    }
}
