package com.gracelink.android.feature.podcast

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.gracelink.android.core.components.GhostButton
import com.gracelink.android.core.components.GoldButton

/**
 * Podcast publishing for pastors/churches. Episode audio can be uploaded
 * directly (via Firebase Storage, which was already a dependency in this
 * project) or added by pasting an existing URL. Series/episode data is
 * fully real and immediately shows up in the Podcasts tab for everyone.
 */
@Composable
fun PodcastCreateScreen(onBack: () -> Unit, vm: PodcastCreateViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var activeSeriesId by remember { mutableStateOf<String?>(null) }

    // Uploading an episode needs a series to belong to first. If you
    // already have one, select it automatically -- previously this
    // stayed null until you tapped an existing series, so returning
    // users saw no upload option at all until they noticed they had to
    // pick one, which read as "there's no way to upload audio."
    LaunchedEffect(state.mySeries) {
        if (activeSeriesId == null) {
            state.mySeries.firstOrNull()?.let { activeSeriesId = it.id }
        }
    }

    var seriesTitle by remember { mutableStateOf("") }
    var seriesDescription by remember { mutableStateOf("") }
    var seriesCategory by remember { mutableStateOf("Teaching") }

    var epTitle by remember { mutableStateOf("") }
    var epUrl by remember { mutableStateOf("") }
    var epDuration by remember { mutableStateOf("") }
    var coverUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> coverUri = uri }

    Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
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
            Text("Podcasts are organized into series -- create one first, then upload episodes to it below", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Field("Series title", seriesTitle) { seriesTitle = it }
            Spacer(Modifier.height(10.dp))
            Field("Description", seriesDescription) { seriesDescription = it }
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { coverPicker.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    if (coverUri != null) {
                        coil.compose.AsyncImage(model = coverUri, contentDescription = "Cover", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    } else {
                        Icon(Icons.Rounded.Add, "Add cover", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(if (coverUri != null) "Cover image selected" else "Add a cover image (optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Teaching", "Worship", "Regional", "Youth").forEach { c ->
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(if (c == seriesCategory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).clickable { seriesCategory = c }.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(c, style = MaterialTheme.typography.labelMedium, color = if (c == seriesCategory) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            GoldButton("Create Series", onClick = {
                vm.createSeries(seriesTitle, seriesDescription, seriesCategory, coverUri) { newId ->
                    activeSeriesId = newId
                    seriesTitle = ""; seriesDescription = ""; coverUri = null
                }
            }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(28.dp))
            Text("My Series", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))
            state.mySeries.forEach { series ->
                val selected = activeSeriesId == series.id
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant).clickable { activeSeriesId = series.id }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(series.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(series.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (activeSeriesId != null) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(20.dp))
                Text("Upload Episode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "To: ${state.mySeries.firstOrNull { it.id == activeSeriesId }?.title ?: "your series"}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(10.dp))
                Field("Episode title", epTitle) { epTitle = it }
                Spacer(Modifier.height(10.dp))
                Field("Duration label (e.g. 28 min)", epDuration) { epDuration = it }
                Spacer(Modifier.height(16.dp))

                val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    if (uri != null) {
                        val id = activeSeriesId
                        if (id != null) {
                            val title = epTitle.ifBlank { "Episode \u2014 ${java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(java.util.Date())}" }
                            vm.addEpisodeFromFile(id, title, uri, epDuration.ifBlank { "\u2014" }) {
                                epTitle = ""; epDuration = ""
                            }
                        }
                    }
                }

                if (state.isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Uploading\u2026", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    GoldButton(
                        "Upload Audio File", icon = Icons.Rounded.Add,
                        onClick = { filePicker.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.uploadError != null) {
                        Text(state.uploadError ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("or paste an audio URL instead", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Field("Audio URL", epUrl) { epUrl = it }
                Spacer(Modifier.height(10.dp))
                GhostButton("Add via URL", onClick = {
                    val id = activeSeriesId ?: return@GhostButton
                    if (epUrl.isNotBlank()) {
                        vm.addEpisode(id, epTitle, epUrl, epDuration.ifBlank { "\u2014" }) {
                            epTitle = ""; epUrl = ""; epDuration = ""
                        }
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
        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(12.dp),
    )
}
