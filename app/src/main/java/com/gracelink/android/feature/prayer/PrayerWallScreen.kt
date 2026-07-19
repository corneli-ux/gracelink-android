package com.gracelink.android.feature.prayer

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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.PrayerGradient
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.PrayerEntity
import org.json.JSONArray

@Composable
fun PrayerWallScreen(onRequireSignIn: () -> Unit = {}, vm: PrayerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Prayer Wall", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                    Text("Stand in the gap for one another", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Gold500).clickable {
                        if (state.isGuest) onRequireSignIn() else vm.showSheet(true)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Add, "New prayer", tint = Color(0xFF1A0F00), modifier = Modifier.size(24.dp))
                }
            }

            // Tabs
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(14.dp)).background(Slate800).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PrayerTab.values().forEach { t ->
                    val selected = state.tab == t
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (selected) Gold500 else Color.Transparent).clickable { vm.setTab(t) }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(t.label, style = MaterialTheme.typography.labelLarge, color = if (selected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.prayers, key = { it.id }) { p ->
                    PrayerCard(p, { vm.togglePrayed(p.id) }, { vm.markAnswered(p.id) }, { vm.encourage(p.id, it) })
                }
            }
        }

        AnimatedVisibility(visible = state.showSheet) {
            SubmitSheet({ text, anon -> vm.submit(text, anon) }, { vm.showSheet(false) })
        }
    }
}

@Composable
private fun PrayerCard(p: PrayerEntity, onPray: () -> Unit, onAnswered: () -> Unit, onEncourage: (String) -> Unit) {
    var showEnc by remember { mutableStateOf(false) }
    var encText by remember { mutableStateOf("") }

    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(if (p.isAnswered) Brush.linearGradient(PrayerGradient) else Brush.verticalGradient(listOf(Slate800, Slate800)))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (p.isAnswered) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = Emerald500, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ANSWERED", style = MaterialTheme.typography.labelSmall, color = Emerald500, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                }
                Text(p.displayName ?: "Anonymous", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Text("• ${fmtTime(p.timestamp)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(p.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)

            // Encouragements
            val encs = parseEncouragements(p.encouragementsJson)
            if (encs.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    encs.take(2).forEach { e ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(4.dp).clip(RoundedCornerShape(2.dp)).background(Emerald500.copy(alpha = 0.6f)))
                            Spacer(Modifier.width(8.dp))
                            Text("${e.first}: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            Text(e.second, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (encs.size > 2) Text("+${encs.size - 2} more", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (p.userPrayedThis) Emerald500.copy(alpha = 0.25f) else Slate900.copy(alpha = 0.7f))
                        .clickable(onClick = onPray).padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (p.userPrayedThis) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (p.userPrayedThis) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("I Prayed • ${p.prayedCount}", style = MaterialTheme.typography.labelMedium, color = if (p.userPrayedThis) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (!p.isAnswered) {
                    Text("Encourage", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showEnc = !showEnc }.padding(horizontal = 8.dp, vertical = 6.dp))
                }
            }

            AnimatedVisibility(visible = showEnc) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(16.dp)).background(Slate900).padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = encText, onValueChange = { encText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Send an encouragement…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold500),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { if (encText.isNotBlank()) { onEncourage(encText.trim()); encText = ""; showEnc = false } })
                    )
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Gold500).clickable { if (encText.isNotBlank()) { onEncourage(encText.trim()); encText = ""; showEnc = false } },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Send, "Send", tint = Color(0xFF1A0F00), modifier = Modifier.size(16.dp))
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
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(20.dp).navigationBarsPadding().imePadding().clickable(enabled = false) {}
        ) {
            Column {
                Text("Share a Prayer", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("All prayers go through moderation before appearing publicly. Be honest and respectful.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    TextField(
                        value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Write your prayer…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold500),
                        minLines = 3,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Post anonymously", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Switch(checked = anon, onCheckedChange = { anon = it }, colors = SwitchDefaults.colors(checkedThumbColor = Gold500, checkedTrackColor = Gold500.copy(alpha = 0.4f), uncheckedTrackColor = Slate800))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(Slate800).clickable(onClick = onDismiss).padding(vertical = 14.dp), contentAlignment = Alignment.Center) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(Gold500).clickable { if (text.isNotBlank()) onSubmit(text.trim(), anon) }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) { Text("Submit Prayer", color = Color(0xFF1A0F00), fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

private fun fmtTime(epoch: Long): String {
    val diff = System.currentTimeMillis() - epoch
    val h = diff / 3_600_000; val d = h / 24
    return when { d > 0 -> "${d}d ago"; h > 0 -> "${h}h ago"; else -> "${diff / 60_000}m ago" }
}

private fun parseEncouragements(json: String): List<Pair<String, String>> = try {
    val arr = JSONArray(json)
    (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        (o.optString("displayName") ?: "Anonymous") to o.optString("text")
    }
} catch (_: Exception) { emptyList() }
