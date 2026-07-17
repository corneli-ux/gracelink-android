package com.gracelink.android.feature.podcast

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary

@Composable
fun PodcastDetailScreen(
    podcastId: String,
    onBack: () -> Unit,
    onPlayEpisode: (String) -> Unit
) {
    // Mock – in real app load by id from repository
    val podcast = remember {
        PodcastUi(
            id = podcastId,
            title = "Grace Daily",
            host = "Pastor Michael",
            description = "Short, powerful daily encouragement rooted in Scripture. New episode every morning.",
            coverUrl = "https://picsum.photos/seed/grace1/600",
            episodeCount = 128
        )
    }
    val episodes = remember {
        listOf(
            EpisodeUi("e1", "The Power of Waiting", "Grace Daily", "28 min", "Today"),
            EpisodeUi("e2", "Walking in Authority", "Grace Daily", "31 min", "Yesterday"),
            EpisodeUi("e3", "Grace for Today", "Grace Daily", "24 min", "2 days ago"),
            EpisodeUi("e4", "The Good Shepherd", "Grace Daily", "29 min", "3 days ago")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                // Cover + info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = podcast.coverUrl,
                        contentDescription = podcast.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = podcast.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = podcast.host,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gold400
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = podcast.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${podcast.episodeCount} episodes",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "EPISODES",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(episodes) { ep ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate900)
                        .clickable { onPlayEpisode(ep.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Gold500.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = Gold400)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ep.title, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                        Text("${ep.duration} • ${ep.date}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
