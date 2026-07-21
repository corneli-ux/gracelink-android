package com.gracelink.android.feature.pastors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.data.db.entity.ArticleEntity
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.PodcastSeriesEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class PastorTab(val label: String) { ABOUT("About"), ARTICLES("Articles"), PODCASTS("Podcasts"), EVENTS("Events") }

@Composable
fun PastorProfileScreen(
    pastorUid: String,
    onBack: () -> Unit,
    onRequireSignIn: () -> Unit = {},
    vm: PastorProfileViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(PastorTab.ABOUT) }
    LaunchedEffect(pastorUid) { vm.load(pastorUid) }
    val pastor = state.pastor
    val isGuest = state.myUid.isBlank()

    Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (pastor == null) {
            Text("Pastor not found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
            return@Column
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text(pastor.displayName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(pastor.beliefSystem.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${state.followerCount} following on Timeline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (state.isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary)
                        .clickable { if (isGuest) onRequireSignIn() else vm.toggleFollow() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        if (state.isFollowing) "Following" else "Follow",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.isFollowing) MaterialTheme.colorScheme.onSurface else Color(0xFF1A0F00),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        ScrollableTabRow(
            selectedTabIndex = tab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 24.dp,
        ) {
            PastorTab.values().forEach { t ->
                Tab(selected = tab == t, onClick = { tab = t }, text = { Text(t.label) })
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when (tab) {
            PastorTab.ABOUT -> Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(pastor.bio ?: "This pastor hasn't added a bio yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            PastorTab.ARTICLES -> ListTab(state.articles, "No articles published yet") { ArticleRow(it) }
            PastorTab.PODCASTS -> ListTab(state.podcasts, "No podcasts published yet") { PodcastRow(it) }
            PastorTab.EVENTS -> ListTab(state.events, "No upcoming events yet") { EventRow(it) }
        }
    }
}

@Composable
private fun <T> ListTab(items: List<T>, emptyText: String, row: @Composable (T) -> Unit) {
    if (items.isEmpty()) {
        Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp)) {
            items(items) { item ->
                row(item)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun ArticleRow(article: ArticleEntity) {
    Column(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(article.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Text(article.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        Spacer(Modifier.height(6.dp))
        Text("${article.likeCount} likes \u00b7 ${article.commentCount} comments", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PodcastRow(series: PodcastSeriesEntity) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Podcasts, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.width(20.dp).height(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(series.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(series.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EventRow(event: ChurchEventEntity) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.width(18.dp).height(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(event.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(
                SimpleDateFormat("EEE, MMM d \u00b7 h:mm a", Locale.getDefault()).format(Date(event.startTime)) + if (event.isOnline) " \u00b7 Online" else "",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
