package com.gracelink.android.feature.prayer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.data.repository.PrayerEncouragement
import com.gracelink.android.data.repository.PrayerRequest

@Composable
fun PrayerWallScreen(onRequireSignIn: () -> Unit = {}, vm: PrayerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingRecordTarget by remember { mutableStateOf<String?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val target = pendingRecordTarget
        pendingRecordTarget = null
        if (granted && target != null) vm.startRecording(target)
    }

    fun requestRecording(prayerId: String) {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            vm.startRecording(prayerId)
        } else {
            pendingRecordTarget = prayerId
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Prayer Wall", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                    Text("Stand in the gap for one another", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary).clickable {
                        if (state.isGuest) onRequireSignIn() else vm.showSheet(true)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Add, "New prayer", tint = Color(0xFF1A0F00), modifier = Modifier.size(24.dp))
                }
            }

            // Tabs
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PrayerTab.values().forEach { t ->
                    val selected = state.tab == t
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { vm.setTab(t) }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(t.label, style = MaterialTheme.typography.labelLarge, color = if (selected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (state.prayers.isEmpty()) {
                Column(Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        when (state.tab) {
                            PrayerTab.MINE -> "You haven't shared a prayer yet"
                            PrayerTab.ANSWERED -> "No answered prayers yet"
                            PrayerTab.ALL -> "No prayers shared yet \u2014 be the first"
                        },
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    items(state.prayers, key = { it.id }) { p ->
                        PrayerCard(
                            p = p,
                            hasPrayed = state.myUid.isNotBlank() && state.myUid in p.prayedByUids,
                            onPray = { vm.togglePrayed(p) },
                            onAnswered = { vm.markAnswered(p.id) },
                            onEncourage = { vm.encourage(p.id, it) },
                            isRecording = state.recordingPrayerId == p.id,
                            isUploading = state.uploadingPrayerId == p.id,
                            onStartRecording = { requestRecording(p.id) },
                            onStopRecording = { vm.stopRecordingAndSend(p.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }

        AnimatedVisibility(visible = state.showSheet) {
            SubmitSheet({ text, anon -> vm.submit(text, anon) }, { vm.showSheet(false) })
        }
    }
}

@Composable
private fun PrayerCard(
    p: PrayerRequest,
    hasPrayed: Boolean,
    onPray: () -> Unit,
    onAnswered: () -> Unit,
    onEncourage: (String) -> Unit,
    isRecording: Boolean,
    isUploading: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
) {
    var showEnc by remember { mutableStateOf(false) }
    var encText by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        if (p.isAnswered) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("ANSWERED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.authorName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Text("\u2022 ${fmtTime(p.timestamp)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(p.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)

            // Encouragements -- real list, not hand-parsed JSON
            if (p.encouragements.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    p.encouragements.take(3).forEach { e ->
                        if (e.audioUrl != null) {
                            AudioEncouragementRow(e.authorName, e.audioUrl)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)))
                                Spacer(Modifier.width(8.dp))
                                Text("${e.authorName}: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                Text(e.text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    if (p.encouragements.size > 3) Text("+${p.encouragements.size - 3} more", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (hasPrayed) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                        .clickable(onClick = onPray).padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (hasPrayed) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (hasPrayed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("I Prayed \u2022 ${p.prayedByUids.size}", style = MaterialTheme.typography.labelMedium, color = if (hasPrayed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (!p.isAnswered) {
                    Text("Encourage", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showEnc = !showEnc }.padding(horizontal = 8.dp, vertical = 6.dp))
                }
            }

            AnimatedVisibility(visible = showEnc) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = encText, onValueChange = { encText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Send an encouragement\u2026", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { if (encText.isNotBlank()) { onEncourage(encText.trim()); encText = ""; showEnc = false } })
                        )
                        Box(
                            Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.primary).clickable { if (encText.isNotBlank()) { onEncourage(encText.trim()); encText = ""; showEnc = false } },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Send, "Send", tint = Color(0xFF1A0F00), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // Voice reply -- pray for someone out loud instead of typing
                    when {
                        isUploading -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Sending voice reply\u2026", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        isRecording -> Row(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.error).clickable(onClick = onStopRecording).padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Stop, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Recording\u2026 tap to stop & send", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                        else -> Row(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.background).clickable(onClick = onStartRecording).padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Mic, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Or pray out loud", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmitSheet(onSubmit: (String, Boolean) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var anon by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)).clickable(onClick = onDismiss)) {
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(MaterialTheme.colorScheme.background).padding(20.dp).navigationBarsPadding().imePadding().clickable(enabled = false) {}
        ) {
            Column {
                Text("Share a Prayer", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("Visible to the whole community. Be honest and respectful.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    TextField(
                        value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Write your prayer\u2026", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary),
                        minLines = 3,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Post anonymously", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Switch(checked = anon, onCheckedChange = { anon = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onDismiss).padding(vertical = 14.dp), contentAlignment = Alignment.Center) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary).clickable { if (text.isNotBlank()) onSubmit(text.trim(), anon) }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) { Text("Submit Prayer", color = Color(0xFF1A0F00), fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun AudioEncouragementRow(authorName: String, audioUrl: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    DisposableEffect(audioUrl) {
        onDispose {
            player?.release()
            player = null
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
            .clickable {
                val current = player
                if (current == null) {
                    val mp = android.media.MediaPlayer()
                    try {
                        mp.setDataSource(audioUrl)
                        mp.setOnCompletionListener {
                            isPlaying = false
                            it.release()
                            player = null
                        }
                        mp.setOnPreparedListener {
                            it.start()
                            isPlaying = true
                        }
                        mp.prepareAsync()
                        player = mp
                    } catch (_: Exception) {
                        isPlaying = false
                    }
                } else if (isPlaying) {
                    current.pause()
                    isPlaying = false
                } else {
                    current.start()
                    isPlaying = true
                }
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
            null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text("$authorName's voice reply", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun fmtTime(epoch: Long): String {
    val diff = System.currentTimeMillis() - epoch
    val h = diff / 3_600_000; val d = h / 24
    return when { d > 0 -> "${d}d ago"; h > 0 -> "${h}h ago"; else -> "${diff / 60_000}m ago" }
}
