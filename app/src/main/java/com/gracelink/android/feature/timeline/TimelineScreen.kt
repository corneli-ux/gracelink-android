package com.gracelink.android.feature.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.data.repository.BiblicalReaction
import com.gracelink.android.data.repository.ReactionSummary
import com.gracelink.android.data.repository.TimelineItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineScreen(
    onOpenArticle: (String) -> Unit = {},
    onOpenPodcast: (String) -> Unit = {},
    onOpenPrayer: () -> Unit = {},
    onOpenEvent: (String) -> Unit = {},
    onOpenQuestion: (String) -> Unit = {},
    onFindChurches: () -> Unit = {},
    onRequireSignIn: () -> Unit = {},
    vm: TimelineViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
        Text(
            "Timeline", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when {
            state.isGuest -> EmptyState(
                "Sign in to see your Timeline", "Follow churches and pastors to see everything they post, all in one feed",
                onRequireSignIn,
            )
            !state.isFollowingAnyone -> EmptyState(
                "Nothing here yet", "Follow a church or pastor from their profile to see their activity here",
                onFindChurches, actionLabel = "Find Churches",
            )
            state.items.isEmpty() -> EmptyState("No recent activity", "Nothing new from who you follow yet", {})
            else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(state.items, key = { "${it.contentType}_${it.contentId}" }) { item ->
                    TimelineCard(
                        item, vm,
                        onOpen = {
                            when (item) {
                                is TimelineItem.Article -> onOpenArticle(item.entity.id)
                                is TimelineItem.Podcast -> onOpenPodcast(item.entity.id)
                                is TimelineItem.Prayer -> onOpenPrayer()
                                is TimelineItem.Event -> onOpenEvent(item.entity.id)
                                is TimelineItem.Question -> onOpenQuestion(item.entity.id)
                            }
                        },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String, onAction: () -> Unit, actionLabel: String? = null) {
    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(40.dp))
        Icon(Icons.Rounded.Timeline, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.height(40.dp).width(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (actionLabel != null) {
            Spacer(Modifier.height(16.dp))
            Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

private fun typeMeta(item: TimelineItem): Triple<ImageVector, String, String> = when (item) {
    is TimelineItem.Article -> Triple(Icons.Rounded.Article, "Article", item.entity.title)
    is TimelineItem.Podcast -> Triple(Icons.Rounded.Podcasts, "New Podcast Series", item.entity.title)
    is TimelineItem.Prayer -> Triple(Icons.Rounded.Spa, "Prayer Request", item.entity.text)
    is TimelineItem.Event -> Triple(Icons.Rounded.CalendarMonth, "Event", item.entity.title)
    is TimelineItem.Question -> Triple(Icons.Rounded.Forum, "Asked the community", item.entity.title)
}

@Composable
private fun TimelineCard(item: TimelineItem, vm: TimelineViewModel, onOpen: () -> Unit) {
    var showComments by remember { mutableStateOf(false) }
    val (icon, kind, title) = typeMeta(item)
    val reactions by vm.reactionsFor(item).collectAsState(initial = ReactionSummary())

    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(14.dp).width(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(kind.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.authorName, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(6.dp))
            Text("\u00b7 ${fmtTime(item.timestamp)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.clickable(onClick = onOpen),
        )
        Spacer(Modifier.height(10.dp))

        // Reaction strip -- in the context of Scripture, not generic emoji
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BiblicalReaction.values().forEach { r ->
                val selected = reactions.myReaction == r
                val count = reactions.counts[r] ?: 0
                Row(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { vm.react(item, r, selected) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(r.emoji, style = MaterialTheme.typography.labelMedium)
                    if (count > 0) {
                        Spacer(Modifier.width(4.dp))
                        Text("$count", style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showComments = !showComments }) {
            Icon(Icons.Rounded.ChatBubbleOutline, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.height(15.dp).width(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (showComments) "Hide comments" else "Comments", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (showComments) {
            Spacer(Modifier.height(8.dp))
            CommentSection(item, vm)
        }
    }
}

@Composable
private fun CommentSection(item: TimelineItem, vm: TimelineViewModel) {
    val comments by vm.commentsFor(item).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }

    Column {
        comments.forEach { c ->
            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(c.authorName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Text(c.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (comments.isEmpty()) {
            Text("No comments yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 6.dp))
        }
        Row(
            Modifier.fillMaxWidth().imePadding().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("Add a comment\u2026", style = MaterialTheme.typography.bodySmall) },
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                textStyle = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Post", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { if (text.isNotBlank()) { vm.addComment(item, text.trim()); text = "" } }.padding(8.dp),
            )
        }
    }
}

private fun fmtTime(epoch: Long): String {
    val diff = System.currentTimeMillis() - epoch
    val h = diff / 3_600_000; val d = h / 24
    return when { d > 6 -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epoch)); d > 0 -> "${d}d"; h > 0 -> "${h}h"; else -> "${(diff / 60_000).coerceAtLeast(1)}m" }
}
