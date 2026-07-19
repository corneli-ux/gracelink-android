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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.VolunteerActivism
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.components.LiveBadge
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.EmberGradient
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.GoldGradient
import com.gracelink.android.core.theme.LiveRed
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage

/**
 * GraceLink's single unified hub. Fully usable as a guest -- the only guest
 * gate is the sign-in banner at the top, which is dismissible via ignoring it
 * (nothing below it requires an account to browse).
 */
@Composable
fun HomeScreen(
    onPlayContent: (String) -> Unit,
    onOpenLiveSession: (String) -> Unit,
    onOpenRadio: () -> Unit,
    onOpenPodcasts: () -> Unit,
    onOpenCommunity: () -> Unit,
    onRequireSignIn: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val isGuest = state.userName.isBlank()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // -- Greeting --------------------------------------------------------
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("GraceLink", style = MaterialTheme.typography.labelMedium, color = Gold400)
                    Text(state.greeting, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        state.userName.ifBlank { "Welcome" },
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Gold500, Emerald500))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.userName.firstOrNull()?.uppercase() ?: "G",
                        color = Color(0xFF1A0F00), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // -- Guest sign-in banner ---------------------------------------------
        if (isGuest) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(GoldGradient))
                        .clickable(onClick = onRequireSignIn)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("You're browsing as a guest", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1A0F00))
                        Text("Sign in to save progress, pray with your church, and more", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1A0F00).copy(alpha = 0.75f))
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = Color(0xFF1A0F00))
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // -- Live Now hero -----------------------------------------------------
        item {
            val live = state.liveRadio.firstOrNull { it.isLive }
            if (live != null) {
                LiveHero(
                    channel = live,
                    hasLiveSession = state.liveSession != null,
                    onPlay = { onPlayContent(live.id) },
                    onJoinConversation = { state.liveSession?.let { onOpenLiveSession(it.id) } },
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // -- Quick actions -------------------------------------------------------
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quickActions) { action ->
                    QuickActionChip(action) {
                        when (action.label) {
                            "Radio" -> onOpenRadio()
                            "Podcasts" -> onOpenPodcasts()
                            "Community" -> onOpenCommunity()
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // -- Continue Listening ---------------------------------------------------
        if (state.continueListening.isNotEmpty()) {
            item {
                SectionHeader("Continue Listening", Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.continueListening) { item ->
                        ContinueCard(item) { onPlayContent(item.id) }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // -- Community CTA -----------------------------------------------------
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Slate800)
                    .clickable(onClick = onOpenCommunity)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Emerald500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Groups, null, tint = Emerald500)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Your Community", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    Text("Churches, prayer, events, and articles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(20.dp))
        }

        // -- Recommended -------------------------------------------------------
        item {
            SectionHeader("Recommended", Modifier.padding(horizontal = 20.dp), "Curated based on your history")
            Spacer(Modifier.height(12.dp))
        }
        items(state.recommended) { item ->
            RecommendedRow(item) { onPlayContent(item.id) }
            Spacer(Modifier.height(10.dp))
        }
    }
}

private data class QuickAction(val label: String, val icon: ImageVector)

private val quickActions = listOf(
    QuickAction("Radio", Icons.Rounded.Radio),
    QuickAction("Podcasts", Icons.Rounded.Podcasts),
    QuickAction("Community", Icons.Rounded.Groups),
)

@Composable
private fun QuickActionChip(action: QuickAction, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Slate800)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(action.icon, null, tint = Gold500, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(action.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LiveHero(
    channel: ContentEntity,
    hasLiveSession: Boolean,
    onPlay: () -> Unit,
    onJoinConversation: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(EmberGradient))
            .clickable(onClick = onPlay)
    ) {
        AsyncImage(
            model = channel.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.85f))))
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveBadge()
                Spacer(Modifier.width(8.dp))
                Text("${channel.listenerCount} listening", style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
            Column {
                Text(
                    channel.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    channel.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(Gold500)
                            .clickable(onClick = onPlay)
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.PlayArrow, null, tint = Color(0xFF1A0F00), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Listen Live", color = Color(0xFF1A0F00), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    if (hasLiveSession) {
                        Spacer(Modifier.width(10.dp))
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .clickable(onClick = onJoinConversation)
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Equalizer, null, tint = LiveRed, modifier = Modifier.size(14.dp))
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
private fun ContinueCard(item: ContentEntity, onClick: () -> Unit) {
    Column(
        Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Slate800)
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = item.thumbnailUrl, contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        }
        Column(Modifier.padding(12.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(item.speaker ?: "GraceLink", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RecommendedRow(item: ContentEntity, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Slate800)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.thumbnailUrl, contentDescription = null,
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LangTag(item.language)
                Spacer(Modifier.width(6.dp))
                TypeTag(item.type.name)
            }
            Spacer(Modifier.height(6.dp))
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(item.speaker ?: "GraceLink", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Gold500.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PlayArrow, "Play", tint = Gold500, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun LangTag(language: ContentLanguage) {
    val color = if (language == ContentLanguage.TE) Gold500 else Emerald500
    Box(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(language.name, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun TypeTag(type: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Slate900)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(type.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
