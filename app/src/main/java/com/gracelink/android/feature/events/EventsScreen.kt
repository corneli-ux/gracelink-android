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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bell
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Login
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
import com.gracelink.android.core.designsystem.components.LiveBadge
import com.gracelink.android.core.designsystem.theme.Emerald500
import com.gracelink.android.core.designsystem.theme.Gold500
import com.gracelink.android.core.designsystem.theme.Slate800
import com.gracelink.android.data.model.LiveSession
import com.gracelink.android.data.model.LiveSessionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Live Events / Debates screen — spec §4.5.
 * Calendar list of upcoming sessions. Tap to see details + Notify Me or Join Queue.
 */
@Composable
fun EventsScreen(
    onOpenLiveSession: (String) -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Text(
            text = "Live Events",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
        )
        Text(
            text = "Debates • Q&A • Worship nights",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, bottom = 16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live now (if any) first
            val live = state.sessions.filter { it.status == LiveSessionStatus.LIVE }
            val upcoming = state.sessions.filter { it.status == LiveSessionStatus.UPCOMING }
                .sortedBy { it.startTime }

            if (live.isNotEmpty()) {
                item {
                    SectionLabel("Live Now")
                    Spacer(Modifier.height(8.dp))
                }
                items(live, key = { it.id }) { session ->
                    EventCard(
                        session = session,
                        onJoin = { onOpenLiveSession(session.id) },
                        onRemind = { viewModel.toggleRemindMe(session.id) },
                        onQueue = { viewModel.toggleJoinQueue(session.id) },
                    )
                }
            }
            if (upcoming.isNotEmpty()) {
                item {
                    SectionLabel("Upcoming")
                    Spacer(Modifier.height(8.dp))
                }
                items(upcoming, key = { it.id }) { session ->
                    EventCard(
                        session = session,
                        onJoin = { onOpenLiveSession(session.id) },
                        onRemind = { viewModel.toggleRemindMe(session.id) },
                        onQueue = { viewModel.toggleJoinQueue(session.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun EventCard(
    session: LiveSession,
    onJoin: () -> Unit,
    onRemind: () -> Unit,
    onQueue: () -> Unit,
) {
    val isLive = session.status == LiveSessionStatus.LIVE
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Slate800)
            .clickable(onClick = onJoin)
    ) {
        AsyncImage(
            model = session.coverImageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.85f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (isLive) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveBadge()
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${session.participantCount} in conversation",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(8.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Gold500, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatDate(session.startTime),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Hosted by ${session.hosts.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Gold500)
                            .clickable(onClick = onJoin)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Login, contentDescription = null, tint = Color(0xFF1A1206), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Join Now", color = Color(0xFF1A1206), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    // Notify me toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (session.remindMe) Gold500.copy(alpha = 0.2f) else Slate800.copy(alpha = 0.6f))
                            .clickable(onClick = onRemind)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Bell,
                                contentDescription = null,
                                tint = if (session.remindMe) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (session.remindMe) "Notifying" else "Notify Me",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (session.remindMe) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (session.joinedQueue) Emerald500.copy(alpha = 0.2f) else Slate800.copy(alpha = 0.6f))
                            .clickable(onClick = onQueue)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (session.joinedQueue) "In Queue" else "Join Queue",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (session.joinedQueue) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(epoch: Long): String {
    val fmt = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
    return fmt.format(Date(epoch))
}
