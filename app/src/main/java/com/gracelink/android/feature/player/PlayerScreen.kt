package com.gracelink.android.feature.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.designsystem.components.GracePrimaryButton
import com.gracelink.android.core.designsystem.components.LiveBadge
import com.gracelink.android.core.designsystem.theme.Emerald500
import com.gracelink.android.core.designsystem.theme.Gold500
import com.gracelink.android.core.designsystem.theme.Slate800
import com.gracelink.android.core.designsystem.theme.Slate900

/**
 * Live / on-demand player screen — spec §4.3.
 *
 * Visual layout:
 *  - Large album art with cross / channel imagery
 *  - Title + speaker + LIVE badge if applicable
 *  - Seek bar with timestamps
 *  - Big play/pause button + skip ±10s/±30s + speed control
 *  - Bottom row: favorite / download / sleep timer / Join Conversation
 *
 * When [onOpenLiveSession] is provided and content is live, tapping the gold
 * "Join Conversation" button reveals the chat panel.
 */
@Composable
fun PlayerScreen(
    contentId: String,
    onBack: () -> Unit,
    onOpenLiveSession: (String) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val content = state.content
    var showChat by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }

    // Load content on first composition
    androidx.compose.runtime.LaunchedEffect(contentId) {
        viewModel.load(contentId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 32.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.weight(1f))
                if (content?.isLive == true) LiveBadge()
            }
            Spacer(Modifier.height(12.dp))

            // Album art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Slate800)
            ) {
                AsyncImage(
                    model = content?.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
                )
                if (state.playerState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = Gold500)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // Title block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Text(
                    text = content?.title ?: "",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = content?.speaker ?: "GraceLink Radio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(20.dp))

            // Seek bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Slider(
                    value = state.playerState.currentPositionMs.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..(state.playerState.durationMs.coerceAtLeast(1L).toFloat()),
                    colors = SliderDefaults.colors(
                        thumbColor = Gold500,
                        activeTrackColor = Gold500,
                        inactiveTrackColor = Slate800,
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(state.playerState.currentPositionMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatTime(state.playerState.durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Transport controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = "Speed",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSpeedSheet = true }
                        .padding(6.dp)
                )
                Icon(
                    Icons.Filled.Replay10,
                    contentDescription = "Back 10s",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            val target = (state.playerState.currentPositionMs - 10_000).coerceAtLeast(0)
                            viewModel.seekTo(target)
                        }
                        .padding(6.dp)
                )
                // Big play/pause
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Gold500, Gold500.copy(alpha = 0.85f))
                            )
                        )
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (state.playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color(0xFF1A1206),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Icon(
                    Icons.Filled.Forward30,
                    contentDescription = "Forward 30s",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            val target = (state.playerState.currentPositionMs + 30_000)
                                .coerceAtMost(state.playerState.durationMs)
                            viewModel.seekTo(target)
                        }
                        .padding(6.dp)
                )
                Icon(
                    Icons.Filled.Bedtime,
                    contentDescription = "Sleep timer",
                    tint = if (state.sleepTimerMinutes != null) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSleepSheet = true }
                        .padding(6.dp)
                )
            }
            Spacer(Modifier.height(20.dp))

            // Secondary actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SecondaryAction(
                    icon = if (state.isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    label = "Favorite",
                    tint = if (state.isFavorite) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { viewModel.toggleFavorite() }
                )
                SecondaryAction(
                    icon = Icons.Filled.Download,
                    label = if (state.isDownloaded) "Downloaded" else "Download",
                    tint = if (state.isDownloaded) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { viewModel.toggleDownload() }
                )
            }

            Spacer(Modifier.weight(1f))

            // Join Conversation gold CTA — only for live content
            if (content?.isLive == true) {
                GracePrimaryButton(
                    text = "Join Conversation",
                    onClick = { showChat = true },
                    leadingIcon = Icons.Filled.Chat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                )
            }
        }

        // Speed picker bottom sheet
        if (showSpeedSheet) {
            SpeedPickerSheet(
                current = state.playerState.playbackSpeed,
                onPick = { viewModel.setSpeed(it); showSpeedSheet = false },
                onDismiss = { showSpeedSheet = false }
            )
        }
        if (showSleepSheet) {
            SleepTimerSheet(
                current = state.sleepTimerMinutes,
                onPick = { viewModel.setSleepTimer(it); showSleepSheet = false },
                onDismiss = { showSleepSheet = false }
            )
        }

        // Chat panel
        AnimatedVisibility(
            visible = showChat,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            // For now, route to the LiveSession screen for full chat.
            // If you'd rather inline chat here, swap with ChatPanel(...).
            content?.let { onOpenLiveSession(it.id) }
        }
    }
}

@Composable
private fun SecondaryAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

@Composable
private fun SpeedPickerSheet(
    current: Float,
    onPick: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Slate900)
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Column {
                Text("Playback Speed", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (s == current) Gold500 else Slate800)
                                .clickable { onPick(s) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text("${s}x", color = if (s == current) Color(0xFF1A1206) else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepTimerSheet(
    current: Int?,
    onPick: (Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Slate900)
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Column {
                Text("Sleep Timer", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(5, 10, 15, 30, 45, 60).forEach { m ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (m == current) Gold500 else Slate800)
                                .clickable { onPick(m) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text("${m}m", color = if (m == current) Color(0xFF1A1206) else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800)
                        .clickable { onPick(null) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Off", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val s = ms / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, sec)
    else String.format("%d:%02d", m, sec)
}
