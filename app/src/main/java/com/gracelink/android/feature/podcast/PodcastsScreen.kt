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
 * Dedicated Podcasts experience – series, episodes, featured rotation,
 * search, and clean unique cards. Completely separate from the old generic library.
 */
@Composable
fun PodcastsScreen(
    onOpenPodcast: (String) -> Unit,
    onPlayEpisode: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    // Mock data – replace with real repository later
    val featured = remember {
        listOf(
            PodcastUi("p1", "Grace Daily", "Pastor Michael", "Daily encouragement", "https://picsum.photos/seed/grace1/400", 128),
            PodcastUi("p2", "Telugu Living Word", "Ps. Raju", "Regional teaching", "https://picsum.photos/seed/grace2/400", 86),
            PodcastUi("p3", "Worship Unplugged", "GraceLink Team", "Live worship sets", "https://picsum.photos/seed/grace3/400", 54)
        )
    }
    val episodes = remember {
        listOf(
            EpisodeUi("e1", "The Power of Waiting", "Grace Daily", "28 min", "Today"),
            EpisodeUi("e2", "Faith in the Storm", "Telugu Living Word", "41 min", "Yesterday"),
            EpisodeUi("e3", "Midnight Worship", "Worship Unplugged", "62 min", "2 days ago"),
            EpisodeUi("e4", "Prayer That Moves Mountains", "Grace Daily", "33 min", "3 days ago"),
            EpisodeUi("e5", "Identity in Christ", "Telugu Living Word", "37 min", "4 days ago")
        )
    }

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
                text = "Sermons • Teaching • Worship • Debates",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search podcasts & episodes…", color = TextMuted) },
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
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Featured carousel
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
                    items(featured) { podcast ->
                        FeaturedPodcastCard(podcast) { onOpenPodcast(podcast.id) }
                    }
                }
            }

            // Latest episodes
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

            items(episodes.filter {
                query.isBlank() || it.title.contains(query, true) || it.show.contains(query, true)
            }) { ep ->
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
            // Play overlay
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
                text = "${episode.show} • ${episode.duration}",
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

// Simple UI models (replace with domain models later)
data class PodcastUi(
    val id: String,
    val title: String,
    val host: String,
    val description: String,
    val coverUrl: String,
    val episodeCount: Int
)

data class EpisodeUi(
    val id: String,
    val title: String,
    val show: String,
    val duration: String,
    val date: String
)
