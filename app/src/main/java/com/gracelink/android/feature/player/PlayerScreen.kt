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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Forward30
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.components.LiveBadge
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900

@Composable
fun PlayerScreen(
    contentId: String,
    onBack: () -> Unit,
    onOpenLiveSession: (String) -> Unit,
    vm: PlayerViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val content = state.content
    var showSpeed by remember { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }

    LaunchedEffect(contentId) { vm.load(contentId) }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background))
        )
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).statusBarsPadding().padding(bottom = 32.dp)) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Slate800).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.weight(1f))
                if (content?.isLive == true) LiveBadge()
            }
            Spacer(Modifier.height(12.dp))

            // Album art
            Box(Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(320.dp).clip(RoundedCornerShape(24.dp)).background(Slate800)) {
                AsyncImage(model = content?.thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))))
                if (state.player.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Gold500) }
                }
            }
            Spacer(Modifier.height(24.dp))

            // Title
            Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
                Text(content?.title ?: "", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Text(content?.speaker ?: "GraceLink Radio", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(20.dp))

            // Seek bar
            Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
                Slider(
                    value = state.player.currentPositionMs.toFloat(),
                    onValueChange = { vm.seekTo(it.toLong()) },
                    valueRange = 0f..state.player.durationMs.coerceAtLeast(1L).toFloat(),
                    colors = SliderDefaults.colors(thumbColor = Gold500, activeTrackColor = Gold500, inactiveTrackColor = Slate800)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(fmtTime(state.player.currentPositionMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(fmtTime(state.player.durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Transport
            Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                Icon(Icons.Rounded.Speed, "Speed", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).clickable { showSpeed = true }.padding(6.dp))
                Icon(Icons.Rounded.Replay10, "Back 10s", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(44.dp).clickable { vm.seekTo((state.player.currentPositionMs - 10_000).coerceAtLeast(0)) }.padding(6.dp))
                Box(
                    Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)).background(Brush.horizontalGradient(listOf(Gold500, Gold500.copy(alpha = 0.85f)))).clickable { vm.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (state.player.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play/Pause", tint = Color(0xFF1A0F00), modifier = Modifier.size(36.dp))
                }
                Icon(Icons.Rounded.Forward30, "Forward 30s", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(44.dp).clickable { vm.seekTo((state.player.currentPositionMs + 30_000).coerceAtMost(state.player.durationMs)) }.padding(6.dp))
                Icon(Icons.Rounded.Bedtime, "Sleep", tint = if (state.sleepTimer != null) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).clickable { showSleep = true }.padding(6.dp))
            }
            Spacer(Modifier.height(20.dp))

            // Secondary
            Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Secondary(if (state.isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, "Favorite", if (state.isFavorite) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant) { vm.toggleFavorite() }
                Secondary(Icons.Rounded.Download, if (state.isDownloaded) "Downloaded" else "Download", if (state.isDownloaded) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant) { vm.download() }
            }

            Spacer(Modifier.weight(1f))
            if (content?.isLive == true) {
                GoldButton("Join Conversation", onClick = { showChat = true }, icon = Icons.Rounded.Chat, modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp))
            }
        }

        if (showSpeed) SpeedSheet(state.player.playbackSpeed, { vm.setSpeed(it); showSpeed = false }, { showSpeed = false })
        if (showSleep) SleepSheet(state.sleepTimer, { vm.setSleepTimer(it); showSleep = false }, { showSleep = false })
        AnimatedVisibility(visible = showChat, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
            content?.let { onOpenLiveSession(it.id) }
        }
    }
}

@Composable
private fun Secondary(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

@Composable
private fun SpeedSheet(current: Float, onPick: (Float) -> Unit, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(onClick = onDismiss)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(24.dp).clickable(enabled = false) {}) {
            Column {
                Text("Playback Speed", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { s ->
                        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(if (s == current) Gold500 else Slate800).clickable { onPick(s) }.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text("${s}x", color = if (s == current) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepSheet(current: Int?, onPick: (Int?) -> Unit, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(onClick = onDismiss)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(24.dp).clickable(enabled = false) {}) {
            Column {
                Text("Sleep Timer", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(5, 10, 15, 30, 45, 60).forEach { m ->
                        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(if (m == current) Gold500 else Slate800).clickable { onPick(m) }.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text("${m}m", color = if (m == current) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Slate800).clickable { onPick(null) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) { Text("Off", color = MaterialTheme.colorScheme.onSurface) }
            }
        }
    }
}

private fun fmtTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val s = ms / 1000; val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, sec) else String.format("%d:%02d", m, sec)
}
