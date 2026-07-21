package com.gracelink.android.feature.events

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Notifications
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
import com.gracelink.android.core.components.LiveBadge
import com.gracelink.android.data.db.entity.LiveSessionEntity
import com.gracelink.android.data.db.entity.LiveSessionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventsScreen(onOpenLiveSession: (String) -> Unit, vm: EventsViewModel = hiltViewModel()) {
    val sessions by vm.sessions.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Text("Live Events", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp))
        Text("Debates • Q&A • Worship nights", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val live = sessions.filter { it.status == LiveSessionStatus.LIVE }
            val upcoming = sessions.filter { it.status == LiveSessionStatus.UPCOMING }.sortedBy { it.startTime }

            if (live.isNotEmpty()) {
                item { SectionLabel("Live Now") }
                items(live, key = { it.id }) { EventCard(it, { onOpenLiveSession(it.id) }, { vm.toggleRemind(it.id) }, { vm.toggleQueue(it.id) }) }
            }
            if (upcoming.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)); SectionLabel("Upcoming") }
                items(upcoming, key = { it.id }) { EventCard(it, { onOpenLiveSession(it.id) }, { vm.toggleRemind(it.id) }, { vm.toggleQueue(it.id) }) }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun EventCard(s: LiveSessionEntity, onJoin: () -> Unit, onRemind: () -> Unit, onQueue: () -> Unit) {
    val isLive = s.status == LiveSessionStatus.LIVE
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onJoin)
    ) {
        AsyncImage(
            model = s.coverImageUrl, contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(140.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        Box(Modifier.fillMaxWidth().height(140.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.88f)))))
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.Bottom) {
            if (isLive) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveBadge()
                    Spacer(Modifier.width(8.dp))
                    Text("${s.participantCount} in conversation", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(fmtDate(s.startTime), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(s.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text("Hosted by ${parseHosts(s.hostsJson).joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLive) {
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary).clickable(onClick = onJoin).padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Login, null, tint = Color(0xFF1A0F00), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Join Now", color = Color(0xFF1A0F00), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(if (s.remindMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).clickable(onClick = onRemind).padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Notifications, null, tint = if (s.remindMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (s.remindMe) "Notifying" else "Notify Me", style = MaterialTheme.typography.labelMedium, color = if (s.remindMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(if (s.joinedQueue) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).clickable(onClick = onQueue).padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (s.joinedQueue) "In Queue" else "Join Queue", style = MaterialTheme.typography.labelMedium, color = if (s.joinedQueue) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun fmtDate(epoch: Long): String = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(epoch))

private fun parseHosts(json: String): List<String> {
    return try {
        org.json.JSONArray(json).let { arr -> (0 until arr.length()).map { arr.getString(it) } }
    } catch (_: Exception) { emptyList() }
}
