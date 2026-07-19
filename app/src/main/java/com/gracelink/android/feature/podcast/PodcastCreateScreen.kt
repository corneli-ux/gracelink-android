package com.gracelink.android.feature.podcast

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800

/**
 * Podcast publishing for pastors/churches. Episode audio is added by URL
 * for now (no on-device upload-to-cloud-storage pipeline exists yet in
 * this app -- that would need its own backend/storage setup) -- but the
 * series/episode data itself is fully real and immediately shows up in
 * the Podcasts tab for everyone.
 */
@Composable
fun PodcastCreateScreen(onBack: () -> Unit, vm: PodcastCreateViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var activeSeriesId by remember { mutableStateOf<String?>(null) }

    var seriesTitle by remember { mutableStateOf("") }
    var seriesDescription by remember { mutableStateOf("") }
    var seriesCategory by remember { mutableStateOf("Teaching") }

    var epTitle by remember { mutableStateOf("") }
    var epUrl by remember { mutableStateOf("") }
    var epDuration by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Obsidian)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Publish a Podcast", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }

        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            if (state.myUid.isBlank()) {
                Text("Set up your profile to publish a podcast.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
                return@Column
            }

            Text("New Series", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))
            Field("Series title", seriesTitle) { seriesTitle = it }
            Spacer(Modifier.height(10.dp))
            Field("Description", seriesDescription) { seriesDescription = it }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Teaching", "Worship", "Regional", "Youth").forEach { c ->
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(if (c == seriesCategory) Gold400 else Slate800).clickable { seriesCategory = c }.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(c, style = MaterialTheme.typography.labelMedium, color = if (c == seriesCategory) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            GoldButton("Create Series", onClick = {
                vm.createSeries(seriesTitle, seriesDescription, seriesCategory) { newId ->
                    activeSeriesId = newId
                    seriesTitle = ""; seriesDescription = ""
                }
            }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(28.dp))
            Text("My Series", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))
            state.mySeries.forEach { series ->
                val selected = activeSeriesId == series.id
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (selected) Gold400.copy(alpha = 0.15f) else Slate800).clickable { activeSeriesId = series.id }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(series.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(series.category, style = MaterialTheme.typography.labelSmall, color = Gold400)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (activeSeriesId != null) {
                Spacer(Modifier.height(20.dp))
                Text("Add Episode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                Field("Episode title", epTitle) { epTitle = it }
                Spacer(Modifier.height(10.dp))
                Field("Audio URL", epUrl) { epUrl = it }
                Spacer(Modifier.height(10.dp))
                Field("Duration label (e.g. 28 min)", epDuration) { epDuration = it }
                Spacer(Modifier.height(16.dp))
                GoldButton("Add Episode", icon = Icons.Rounded.Add, onClick = {
                    val id = activeSeriesId ?: return@GoldButton
                    vm.addEpisode(id, epTitle, epUrl, epDuration.ifBlank { "\u2014" }) {
                        epTitle = ""; epUrl = ""; epDuration = ""
                    }
                }, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(label) },
        colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400),
        shape = RoundedCornerShape(12.dp),
    )
}
