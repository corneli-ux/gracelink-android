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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.Spa
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
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate850
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary

/**
 * Community hub -- single clean entry for Churches, Prayer Wall, Events,
 * Articles, Faith. Churches gets a full-width featured row since joining a
 * church is the anchor action here; the rest sit in a 2-column grid.
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
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Community",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(
            text = "Belong \u2022 Pray \u2022 Grow together",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Featured: Churches (the anchor action for this hub)
        FeaturedTile(
            title = "Find Your Church",
            subtitle = "Join a church family and stay connected to your community",
            icon = Icons.Rounded.Church,
            onClick = onOpenChurches,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Grow", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = TextSecondary)
        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(260.dp),
        ) {
            items(gridItems(onOpenPrayer, onOpenEvents, onOpenArticles, onOpenFaith)) { item ->
                GridTile(item)
            }
        }
    }
}

private data class CommunityGridItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit,
)

private fun gridItems(
    onOpenPrayer: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenArticles: () -> Unit,
    onOpenFaith: () -> Unit,
): List<CommunityGridItem> = listOf(
    CommunityGridItem("Prayer Wall", "Share & encourage", Icons.Rounded.Spa, Emerald500, onOpenPrayer),
    CommunityGridItem("Events", "Live & upcoming", Icons.Rounded.CalendarMonth, Gold400, onOpenEvents),
    CommunityGridItem("Articles", "Devotionals & teaching", Icons.Rounded.Article, Color(0xFF5AB8E0), onOpenArticles),
    CommunityGridItem("Faith Journey", "Growth & milestones", Icons.Rounded.AutoStories, Color(0xFFB89CD9), onOpenFaith),
)

@Composable
private fun FeaturedTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(Gold500.copy(alpha = 0.22f), Slate850)))
            .border(1.dp, Gold500.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Gold500.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Gold500, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun GridTile(item: CommunityGridItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Slate850)
            .border(1.dp, item.accent.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .clickable(onClick = item.onClick)
            .padding(16.dp)
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(item.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, null, tint = item.accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}
