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
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.LiveRed
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate700
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary
import com.gracelink.android.data.db.entity.ContentEntity

/**
 * Faith Link's home dashboard.
 *
 * Deliberately flat: no gradient hero boxes, no colored card soup. Typography
 * carries the hierarchy, hairline dividers separate sections, and the single
 * accent color (gold) is reserved for the live indicator and the play glyph
 * so it still reads as "premium" rather than plain.
 */
@Composable
fun HomeScreen(
    onPlayContent: (String) -> Unit,
    onOpenLiveSession: (String) -> Unit,
    onOpenRadio: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val live = state.liveRadio.firstOrNull { it.isLive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        // -- Header --------------------------------------------------------
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(
                    "FAITH LINK",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    state.greeting.ifBlank { "Welcome" } + (state.userName.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                )
            }
        }

        // -- Verse of the Day -------------------------------------------------
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val verse = remember { com.gracelink.android.core.BibleVerseProvider.verseOfTheDay(context) }
            if (verse != null) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)) {
                    Text("VERSE OF THE DAY", style = MaterialTheme.typography.labelSmall, color = Gold500, letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("\u201c${verse.text}\u201d", style = MaterialTheme.typography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("\u2014 ${verse.reference}", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Slate700, thickness = 0.75.dp)
            }
        }

        // -- Live strip (flat, no box) --------------------------------------
        if (live != null) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPlayContent(live.id) }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(LiveRed))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("LIVE NOW", style = MaterialTheme.typography.labelSmall, color = LiveRed, fontWeight = FontWeight.Bold)
                        Text(live.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (state.liveSession != null) {
                        Text(
                            "Join",
                            style = MaterialTheme.typography.labelLarge,
                            color = Gold500,
                            modifier = Modifier.clickable { state.liveSession?.let { onOpenLiveSession(it.id) } }
                        )
                    } else {
                        Icon(Icons.Outlined.PlayCircleOutline, null, tint = Gold500, modifier = Modifier.size(28.dp))
                    }
                }
                HorizontalDivider(color = Slate700, thickness = 0.75.dp)
            }
        }

        // -- Radio row -- the only entry point to Radio when nothing is
        // currently live (Podcasts and Community already have their own
        // bottom-nav tabs, so repeating them here was just duplication) ----
        item {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onOpenRadio).padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Radio, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Radio Schedule", style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(14.dp))
            }
            HorizontalDivider(color = Slate700, thickness = 0.75.dp)
        }

        // -- Continue Listening ---------------------------------------------
        if (state.continueListening.isNotEmpty()) {
            item {
                SectionHeader("Continue Listening")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(state.continueListening) { item ->
                        ContinueItem(item) { onPlayContent(item.id) }
                    }
                }
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Slate700, thickness = 0.75.dp)
            }
        }

        // -- Recommended: plain rows, divider-separated ----------------------
        item { SectionHeader("Recommended") }
        items(state.recommended.size) { index ->
            val item = state.recommended[index]
            RecommendedRow(item) { onPlayContent(item.id) }
            if (index != state.recommended.lastIndex) {
                HorizontalDivider(color = Slate700, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    )
}

@Composable
private fun ContinueItem(item: ContentEntity, onClick: () -> Unit) {
    Column(
        Modifier.width(140.dp).clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.thumbnailUrl, contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(10.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        Spacer(Modifier.height(10.dp))
        Text(item.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(item.speaker ?: "Faith Link", style = MaterialTheme.typography.bodySmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RecommendedRow(item: ContentEntity, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.thumbnailUrl, contentDescription = null,
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(item.speaker ?: "Faith Link", style = MaterialTheme.typography.bodySmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(14.dp))
    }
}
