package com.gracelink.android.feature.podcast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate850
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary
import com.gracelink.android.data.db.entity.PodcastEpisodeEntity
import com.gracelink.android.data.db.entity.PodcastSeriesEntity

/**
 * Real podcast data (Room-backed via PodcastRepository) -- pastors and
 * churches publish here from Pastor Studio / Church Portal and it shows
 * up immediately for everyone.
 */
@Composable
fun PodcastsScreen(
    onOpenPodcast: (String) -> Unit,
    onPlayEpisode: (String) -> Unit,
    vm: PodcastsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    val categories = listOf("All") + state.series.map { it.category }.distinct()
    val filteredSeries = state.series.filter { category == "All" || it.category == category }
    val seriesTitleById = state.series.associateBy({ it.id }, { it.title })
    val filteredEpisodes = state.episodes.filter { ep ->
        val seriesCategory = state.series.firstOrNull { it.id == ep.podcastId }?.category
        (category == "All" || seriesCategory == category) &&
            (query.isBlank() || ep.title.contains(query, true) || (seriesTitleById[ep.podcastId]?.contains(query, true) == true))
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().background(Obsidian)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Podcasts", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Text("Sermons \u2022 Teaching \u2022 Worship \u2022 Debates", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search podcasts & episodes\u2026", color = TextMuted) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold500.copy(alpha = 0.5f), unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedContainerColor = Slate900, unfocusedContainerColor = Slate900,
                    cursorColor = Gold400, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                )
            )
            if (categories.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { c ->
                        val selected = c == category
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) Gold500 else Slate900).clickable { category = c }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(c, style = MaterialTheme.typography.labelMedium, color = if (selected) Obsidian else TextSecondary)
                        }
                    }
                }
            }
        }

        if (state.series.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Podcasts, null, tint = Gold500.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(10.dp))
                Text("No podcasts published yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("Pastors and churches can publish from their portal", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (filteredSeries.isNotEmpty()) {
                item {
                    Text("SERIES", style = MaterialTheme.typography.labelMedium, color = TextMuted, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), letterSpacing = 1.2.sp)
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(filteredSeries, key = { it.id }) { series -> SeriesCard(series) { onOpenPodcast(series.id) } }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text("LATEST EPISODES", style = MaterialTheme.typography.labelMedium, color = TextMuted, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), letterSpacing = 1.2.sp)
            }
            if (filteredEpisodes.isEmpty()) {
                item { Text("No episodes match", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) }
            }
            items(filteredEpisodes, key = { it.id }) { ep ->
                EpisodeRow(ep, seriesTitleById[ep.podcastId] ?: "Faith Link") { onPlayEpisode(ep.id) }
            }
        }
    }
}

@Composable
private fun SeriesCard(series: PodcastSeriesEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(160.dp).clip(RoundedCornerShape(18.dp)).background(Slate850)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp)).clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Slate800)) {
            if (series.coverUrl != null) {
                AsyncImage(model = series.coverUrl, contentDescription = series.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Podcasts, null, tint = Gold500.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                }
            }
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp).size(36.dp).clip(CircleShape).background(Gold500),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.PlayArrow, null, tint = Obsidian, modifier = Modifier.size(22.dp)) }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(series.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(series.authorName, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            Text(series.category, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
private fun EpisodeRow(episode: PodcastEpisodeEntity, seriesTitle: String, onPlay: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).clip(RoundedCornerShape(14.dp)).background(Slate900).clickable(onClick = onPlay).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Gold500.copy(alpha = 0.25f), Slate800))),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.PlayArrow, null, tint = Gold400, modifier = Modifier.size(26.dp)) }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(episode.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$seriesTitle \u2022 ${episode.durationLabel}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
