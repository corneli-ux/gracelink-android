package com.gracelink.android.feature.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate850
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary

/**
 * Community hub – single clean entry for Churches, Prayer Wall, Events, Articles, Faith.
 * Fixes the previous scattered bottom-nav overload.
 */
@Composable
fun CommunityScreen(
    onOpenChurches: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenArticles: () -> Unit,
    onOpenFaith: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Community",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(
            text = "Belong • Pray • Grow together",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        CommunityTile(
            title = "Churches",
            subtitle = "Find & join your church family • Membership",
            icon = Icons.Rounded.Church,
            accent = Gold500,
            onClick = onOpenChurches
        )

        Spacer(modifier = Modifier.height(12.dp))

        CommunityTile(
            title = "Prayer Wall",
            subtitle = "Share requests • Encourage one another",
            icon = Icons.Rounded.Spa,
            accent = Emerald500,
            onClick = onOpenPrayer
        )

        Spacer(modifier = Modifier.height(12.dp))

        CommunityTile(
            title = "Events",
            subtitle = "Live sessions • Upcoming gatherings",
            icon = Icons.Rounded.CalendarMonth,
            accent = Gold400,
            onClick = onOpenEvents
        )

        Spacer(modifier = Modifier.height(12.dp))

        CommunityTile(
            title = "Articles & Teaching",
            subtitle = "Written devotionals and deeper study",
            icon = Icons.Rounded.Article,
            accent = Color(0xFF5AB8E0),
            onClick = onOpenArticles
        )

        Spacer(modifier = Modifier.height(12.dp))

        CommunityTile(
            title = "Faith Journey",
            subtitle = "Personal growth path & milestones",
            icon = Icons.Rounded.AutoStories,
            accent = Color(0xFFB89CD9),
            onClick = onOpenFaith
        )
    }
}

@Composable
private fun CommunityTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Slate850)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(accent.copy(alpha = 0.35f), Color.Transparent)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
