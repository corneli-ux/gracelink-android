package com.gracelink.android.feature.home

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.designsystem.components.GraceCard
import com.gracelink.android.core.designsystem.components.GraceSectionHeader
import com.gracelink.android.core.designsystem.components.LiveBadge
import com.gracelink.android.core.designsystem.theme.Emerald500
import com.gracelink.android.core.designsystem.theme.Gold500
import com.gracelink.android.core.designsystem.theme.GraceGradients
import com.gracelink.android.core.designsystem.theme.LiveRed
import com.gracelink.android.core.designsystem.theme.Slate800
import com.gracelink.android.data.model.ContentCategory
import com.gracelink.android.data.model.ContentItem
import com.gracelink.android.data.model.ContentLanguage

/**
 * Home / Discover — spec §4.2.
 *
 * Layout:
 *   1. Personalized greeting (top, large)
 *   2. Live Now banner (prominent — gold-bordered, pulsing)
 *   3. Horizontal category chips (Worship, Sermons, Debates, Regional, Youth, Testimony)
 *   4. Continue Listening horizontal list (with progress bars)
 *   5. Recommended section (vertical cards)
 */
@Composable
fun HomeScreen(
    onPlayContent: (String) -> Unit,
    onOpenLiveSession: (String) -> Unit,
    onSeeAll: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // ── Greeting header ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = state.greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.userName.ifBlank { "Welcome to GraceLink" },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Gold500, Emerald500))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.userName.firstOrNull()?.uppercase() ?: "G",
                        color = Color(0xFF1A1206),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // ── Live Now banner ──────────────────────────────────────────────────
        item {
            val liveChannel = state.home.liveRadio.firstOrNull { it.isLive }
            if (liveChannel != null) {
                LiveNowBanner(
                    channel = liveChannel,
                    onClick = { onPlayContent(liveChannel.id) },
                    onJoinConversation = state.liveSessionId?.let { { onOpenLiveSession(it) } }
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── Category chips ───────────────────────────────────────────────────
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ContentCategory.values()) { cat ->
                    CategoryChip(category = cat) { onSeeAll() }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── Continue Listening ───────────────────────────────────────────────
        if (state.home.continueListening.isNotEmpty()) {
            item {
                GraceSectionHeader(
                    title = "Continue Listening",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    trailingText = "See all",
                    onTrailingClick = onSeeAll
                )
                Spacer(Modifier.height(12.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.home.continueListening) { (item, progressMs) ->
                        ContinueListeningCard(
                            item = item,
                            progressMs = progressMs,
                            onClick = { onPlayContent(item.id) }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Recommended ──────────────────────────────────────────────────────
        item {
            GraceSectionHeader(
                title = "Recommended for You",
                modifier = Modifier.padding(horizontal = 20.dp),
                subtitle = "Curated based on your listening history"
            )
            Spacer(Modifier.height(12.dp))
        }
        items(state.home.recommended) { item ->
            RecommendedCard(item = item, onClick = { onPlayContent(item.id) })
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LiveNowBanner(
    channel: ContentItem,
    onClick: () -> Unit,
    onJoinConversation: (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GraceGradients.liveCard())
            .clickable(onClick = onClick)
    ) {
        // Cover image
        AsyncImage(
            model = channel.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        // Scrim for legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.85f))))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveBadge()
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${channel.listenerCount} listening",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }

            Column {
                Text(
                    text = channel.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = channel.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Gold500)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color(0xFF1A1206), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Listen Live", color = Color(0xFF1A1206), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    if (onJoinConversation != null) {
                        Spacer(Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .clickable(onClick = onJoinConversation)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Equalizer, contentDescription = null, tint = LiveRed, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Join Conversation", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: ContentCategory, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Slate800)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Gold500,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ContinueListeningCard(
    item: ContentItem,
    progressMs: Long,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Slate800)
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
            // Progress bar overlay
            val progress = if (item.durationMs > 0) (progressMs.toFloat() / item.durationMs).coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomStart)
                    .background(Gold500.copy(alpha = progress))
            )
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.speaker ?: "GraceLink",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RecommendedCard(
    item: ContentItem,
    onClick: () -> Unit,
) {
    GraceCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        cornerRadius = 16.dp,
    ) {
        Row {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LanguageTag(item.language)
                    Spacer(Modifier.width(6.dp))
                    TypeTag(item.type.name)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.speaker ?: "GraceLink",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Gold500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Gold500, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun LanguageTag(language: ContentLanguage) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(when (language) {
                ContentLanguage.EN -> Emerald500.copy(alpha = 0.15f)
                ContentLanguage.TE -> Gold500.copy(alpha = 0.15f)
            })
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = language.name,
            style = MaterialTheme.typography.labelSmall,
            color = when (language) {
                ContentLanguage.EN -> Emerald500
                ContentLanguage.TE -> Gold500
            }
        )
    }
}

@Composable
private fun TypeTag(type: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Slate800)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = type.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
