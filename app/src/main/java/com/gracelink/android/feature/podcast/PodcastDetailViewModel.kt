package com.gracelink.android.feature.podcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.PodcastEpisodeEntity
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import com.gracelink.android.data.repository.PodcastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PodcastDetailState(
    val series: PodcastSeriesEntity? = null,
    val episodes: List<PodcastEpisodeEntity> = emptyList(),
)

@HiltViewModel
class PodcastDetailViewModel @Inject constructor(
    private val repo: PodcastRepository,
) : ViewModel() {

    private val podcastId = MutableStateFlow("")
    private val seriesFlow = MutableStateFlow<PodcastSeriesEntity?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<PodcastDetailState> = combine(
        seriesFlow,
        podcastId.flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.episodesFor(id) }
    ) { series, episodes ->
        PodcastDetailState(series, episodes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PodcastDetailState())

    fun load(id: String) {
        podcastId.value = id
        viewModelScope.launch { seriesFlow.value = repo.seriesById(id) }
    }
}
