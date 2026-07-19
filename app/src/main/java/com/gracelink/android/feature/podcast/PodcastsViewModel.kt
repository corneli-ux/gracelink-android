package com.gracelink.android.feature.podcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracelink.android.data.db.entity.PodcastEpisodeEntity
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import com.gracelink.android.data.repository.PodcastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PodcastsListState(
    val series: List<PodcastSeriesEntity> = emptyList(),
    val episodes: List<PodcastEpisodeEntity> = emptyList(),
)

@HiltViewModel
class PodcastsViewModel @Inject constructor(
    repo: PodcastRepository,
) : ViewModel() {
    val state: StateFlow<PodcastsListState> = combine(repo.allSeries(), repo.allEpisodes()) { series, episodes ->
        PodcastsListState(series, episodes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PodcastsListState())
}
