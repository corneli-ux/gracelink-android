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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

/**
 * Dedicated Podcasts experience -- series, episodes, featured rotation,
 * search + category filter, distinct from the old generic library.
 *
 * NOTE: podcast/episode data below is still the placeholder set from the
 * previous build (no ContentRepository wiring for podcasts yet) -- flagging
 * this as a known gap rather than pretending it's live data.
 */
@Composable
fun PodcastsScreen(
    onOpenPodcast: (String) -> Unit,
    onPlayEpisode: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    val featured = remember {
        listOf(
            PodcastUi("p1", "Grace Daily", "Pastor Michael", "Daily encouragement", "https://picsum.photos/seed/grace1/400", 128, "Teaching"),
            PodcastUi("p2", "Telugu Living Word", "Ps. Raju", "Regional teaching", "https://picsum.photos/seed/grace2/400", 86, "Regional"),
            PodcastUi("p3", "Worship Unplugged", "GraceLink Team", "Live worship sets", "https://picsum.photos/seed/grace3/400", 54, "Worship")
        )
    }
    val episodes = remember {
        listOf(
            EpisodeUi("e1", "The Power of Waiting", "Grace Daily", "28 min", "Today", "Teaching"),
            EpisodeUi("e2", "Faith in the Storm", "Telugu Living Word", "41 min", "Yesterday", "Regional"),
            EpisodeUi("e3", "Midnight Worship", "Worship Unplugged", "62 min", "2 days ago", "Worship"),
            EpisodeUi("e4", "Prayer That Moves Mountains", "Grace Daily", "33 min", "3 days ago", "Teaching"),
            EpisodeUi("e5", "Identity in Christ", "Telugu Living Word", "37 min", "4 days ago", "Regional")
        )
    }
    val categories = listOf("All", "Teaching", "Worship", "Regional")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = "Podcasts",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Sermons \u2022 Teaching \u2022 Worship \u2022 Debates",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search podcasts & episodes\u2026", color = TextMuted) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold500.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    cursorColor = Gold400,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { c ->
                    val selected = c == category
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) Gold500 else Slate900)
                            .clickable { category = c }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(c, style = MaterialTheme.typography.labelMedium, color = if (selected) Obsidian else TextSecondary)
                    }
                }
            }
        }

        val filteredFeatured = featured.filter { category == "All" || it.category == category }
        val filteredEpisodes = episodes.filter {
            (category == "All" || it.category == category) &&
                (query.isBlank() || it.title.contains(query, true) || it.show.contains(query, true))
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredFeatured.isNotEmpty()) {
                item {
                    Text(
                        text = "FEATURED SERIES",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        letterSpacing = 1.2.sp
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredFeatured) { podcast ->
                            FeaturedPodcastCard(podcast) { onOpenPodcast(podcast.id) }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "LATEST EPISODES",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    letterSpacing = 1.2.sp
                )
            }

            if (filteredEpisodes.isEmpty()) {
                item {
                    Text(
                        "No episodes match",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }

            items(filteredEpisodes) { ep ->
                EpisodeRow(ep) { onPlayEpisode(ep.id) }
            }
        }
    }
}

@Composable
private fun FeaturedPodcastCard(podcast: PodcastUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Slate850)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Slate800)
        ) {
            AsyncImage(
                model = podcast.coverUrl,
                contentDescription = podcast.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Gold500),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = Obsidian, modifier = Modifier.size(22.dp))
            }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = podcast.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = podcast.host,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = "${podcast.episodeCount} episodes",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun EpisodeRow(episode: EpisodeUi, onPlay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Slate900)
            .clickable(onClick = onPlay)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(listOf(Gold500.copy(alpha = 0.25f), Slate800))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PlayArrow, null, tint = Gold400, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${episode.show} \u2022 ${episode.duration}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = episode.date,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

// Simple UI models (replace with domain models once podcasts have a real repository)
data class PodcastUi(
    val id: String,
    val title: String,
    val host: String,
    val description: String,
    val coverUrl: String,
    val episodeCount: Int,
    val category: String = "All",
)

data class EpisodeUi(
    val id: String,
    val title: String,
    val show: String,
    val duration: String,
    val date: String,
    val category: String = "All",
)
