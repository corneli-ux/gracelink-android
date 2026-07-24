package com.gracelink.android.feature.community

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gracelink.android.core.components.GlassCard
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary

private data class CommunityItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val accentColor: Color = Gold400,
)

private val communityItems = listOf(
    CommunityItem(Icons.Rounded.Church, "Churches", "Find and join a church family"),
    CommunityItem(Icons.Rounded.Spa, "Prayer Wall", "Share requests, pray for others"),
    CommunityItem(Icons.Rounded.CalendarMonth, "Events", "Live and upcoming gatherings"),
    CommunityItem(Icons.Rounded.Article, "Articles", "Devotionals and teaching"),
    CommunityItem(Icons.Rounded.AutoStories, "Faith Journey", "Track your growth"),
    CommunityItem(Icons.Rounded.Forum, "Forum", "Ask questions, help answer others"),
    CommunityItem(Icons.Rounded.Timeline, "Timeline", "Follow churches and pastors"),
)

@Composable
fun CommunityScreen(
    onOpenChurches: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenArticles: () -> Unit,
    onOpenFaith: () -> Unit,
    onOpenForum: () -> Unit,
    onOpenTimeline: () -> Unit = {},
) {
    val onClicks = listOf(onOpenChurches, onOpenPrayer, onOpenEvents, onOpenArticles, onOpenFaith, onOpenForum, onOpenTimeline)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text("Community", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Text("Belong \u00b7 Pray \u00b7 Grow together", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(communityItems.size) { index ->
                val item = communityItems[index]
                CommunityCard(item, onClicks[index])
            }
        }
    }
}

@Composable
private fun CommunityCard(item: CommunityItem, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
