package com.gracelink.android.feature.listen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.components.GlassCard
import com.gracelink.android.core.components.LiveBadge
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import com.gracelink.android.data.db.entity.PodcastEpisodeEntity
import com.gracelink.android.feature.podcast.PodcastsViewModel
import com.gracelink.android.feature.audioconnect.AudioConnectViewModel
import com.gracelink.android.feature.audioconnect.AudioConnectState

/**
 * Merged Listen tab — one place for podcasts, radio, and live spaces.
 */
@Composable
fun ListenTabScreen(
    onOpenPodcast: (String) -> Unit,
    onPlayEpisode: (String) -> Unit,
    onOpenRadio: () -> Unit,
    onOpenLiveSpaces: () -> Unit,
    podcastVm: PodcastsViewModel = hiltViewModel(),
    audioVm: AudioConnectViewModel = hiltViewModel(),
) {
    val podcastState by podcastVm.state.collectAsStateWithLifecycle()
    val audioState by audioVm.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Listen", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text("Sermons · Radio · Live Spaces", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        // Quick action row — Radio + Live Spaces
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassCard(modifier = Modifier.weight(1f).clickable(onClick = onOpenRadio)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Radio, null, tint = Gold400, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Radio", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("24/7 Live", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
            GlassCard(modifier = Modifier.weight(1f).clickable(onClick = onOpenLiveSpaces)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Headphones, null, tint = Gold400, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        val activeSpaces = audioState.spaces.size
                        Text("Live Spaces", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text(if (activeSpaces > 0) "$activeSpaces active" else "Start one", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Active live spaces
        if (audioState.spaces.isNotEmpty()) {
            audioState.spaces.forEach { space ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp).clickable { onOpenLiveSpaces() }
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        LiveBadge()
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(space.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Hosted by ${space.hostName} \u00b7 ${space.participantCount} listening", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Box(Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary).padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Join", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1A0F00))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Search
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            placeholder = { Text("Search podcasts & episodes\u2026", color = TextMuted) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextMuted) },
            singleLine = true, shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), unfocusedBorderColor = Color.White.copy(alpha = 0.06f),
                focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background,
                cursorColor = MaterialTheme.colorScheme.primary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
            ),
        )
        Spacer(Modifier.height(16.dp))

        // Podcast series
        if (podcastState.series.isNotEmpty()) {
            Text("SERIES", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextMuted, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), letterSpacing = 1.2.sp)
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(podcastState.series, key = { it.id }) { series -> SeriesCard(series) { onOpenPodcast(series.id) } }
            }
            Spacer(Modifier.height(20.dp))
        }

        // Latest episodes
        val filteredEpisodes = podcastState.episodes.filter { ep -> query.isBlank() || ep.title.contains(query, true) }
        Text("LATEST EPISODES", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextMuted, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), letterSpacing = 1.2.sp)
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (filteredEpisodes.isEmpty()) {
                item {
                    Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Podcasts, null, tint = Gold400.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("No episodes match", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }
            items(filteredEpisodes, key = { it.id }) { ep ->
                val seriesTitle = podcastState.series.firstOrNull { it.id == ep.podcastId }?.title ?: "GraceLink"
                EpisodeRow(ep, seriesTitle) { onPlayEpisode(ep.id) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 24.dp))
            }
        }
    }
}

@Composable
private fun SeriesCard(series: PodcastSeriesEntity, onClick: () -> Unit) {
    Column(modifier = Modifier.width(170.dp).clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick)) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (series.coverUrl != null) { AsyncImage(model = series.coverUrl, contentDescription = series.title, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            else { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Podcasts, null, tint = Gold400.copy(alpha = 0.5f), modifier = Modifier.size(36.dp)) } }
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(40.dp).shadow(6.dp, CircleShape, ambientColor = Gold400.copy(alpha = 0.2f)).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Icon(androidx.compose.material.icons.rounded.PlayArrow, null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(24.dp))
            }
        }
        Column(modifier = Modifier.padding(14.dp)) {
            Text(series.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(series.authorName, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            Text(series.category, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
private fun EpisodeRow(episode: PodcastEpisodeEntity, seriesTitle: String, onPlay: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onPlay).padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Icon(androidx.compose.material.icons.rounded.PlayArrow, null, tint = Gold400, modifier = Modifier.size(24.dp)) }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(episode.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$seriesTitle \u00b7 ${episode.durationLabel}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
