package com.gracelink.android.feature.library

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
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.ContentEntity
import com.gracelink.android.data.db.entity.ContentLanguage
import com.gracelink.android.data.db.entity.ContentType

@Composable
fun LibraryScreen(onPlayContent: (String) -> Unit, vm: LibraryViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Text(
            "Library",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp)
        )
        Text(
            "Sermons • Podcasts • Debates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
        )

        // Search
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Slate800)
        ) {
            TextField(
                value = state.query,
                onValueChange = vm::setQuery,
                placeholder = { Text("Search sermons, podcasts, debates…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Gold500,
                ),
            )
        }

        // Filters
        FilterRow("All", state.category == null) { vm.setCategory(null) }
        Spacer(Modifier.height(4.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ContentCategory.values()) { cat ->
                Chip(cat.name.lowercase().replaceFirstChar { it.uppercase() }, state.category == cat) {
                    vm.setCategory(if (state.category == cat) null else cat)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Chip("EN+TE", state.language == null) { vm.setLanguage(null) } }
            items(ContentLanguage.values()) { lang ->
                Chip(lang.name, state.language == lang) {
                    vm.setLanguage(if (state.language == lang) null else lang)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Chip("All types", state.type == null) { vm.setType(null) } }
            items(ContentType.values()) { t ->
                Chip(t.name.lowercase().replaceFirstChar { it.uppercase() }, state.type == t) {
                    vm.setType(if (state.type == t) null else t)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("${state.items.size} items", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.items, key = { it.id }) { item ->
                LibraryRow(item) { onPlayContent(item.id) }
            }
        }
    }
}

@Composable
private fun FilterRow(label: String, selected: Boolean, onClick: () -> Unit) { /* kept for symmetry */ }

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Gold500 else Slate800)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun LibraryRow(item: ContentEntity, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate800)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = item.thumbnailUrl, contentDescription = null,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LangTag(item.language)
                Spacer(Modifier.width(6.dp))
                DurationTag(item.durationMs)
            }
            Spacer(Modifier.height(4.dp))
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(item.speaker ?: "GraceLink", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            if (item.isDownloadable) Icons.Rounded.DownloadDone else Icons.Rounded.Download,
            "Download",
            tint = if (item.isDownloadable) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable { }.padding(10.dp)
        )
    }
}

@Composable
private fun LangTag(language: ContentLanguage) {
    val color = if (language == ContentLanguage.TE) Gold500 else Emerald500
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(language.name, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun DurationTag(ms: Long) {
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Slate900.copy(alpha = 0.7f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text("${ms / 60_000} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
