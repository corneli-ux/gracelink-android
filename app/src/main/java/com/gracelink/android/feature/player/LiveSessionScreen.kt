package com.gracelink.android.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gracelink.android.core.components.LiveBadge
import com.gracelink.android.data.repository.LiveChatMessage
import org.json.JSONArray

@Composable
fun LiveSessionScreen(sessionId: String, onBack: () -> Unit, vm: LiveSessionViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(sessionId) { vm.load(sessionId) }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    val session = state.session

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().imePadding()) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(12.dp))
            LiveBadge()
            Spacer(Modifier.width(8.dp))
            Text("${session?.participantCount ?: 0} listening", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Session header
        session?.let { s ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                AsyncImage(model = s.coverImageUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text("Hosted by ${parseHosts(s.hostsJson).joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(s.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // Chat header
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Live Chat", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (state.isQuestionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).clickable { vm.toggleQuestionMode() }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(if (state.isQuestionMode) "Question Mode: ON" else "Ask a Question", style = MaterialTheme.typography.labelMedium, color = if (state.isQuestionMode) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface)
            }
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("Be the first to say something", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(state.messages, key = { it.id }) { msg -> ChatBubble(msg, isMine = msg.userId == state.myUid) }
        }

        // Input
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input, onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (state.isQuestionMode) "Ask your question…" else "Send a message…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (input.isNotBlank()) { vm.sendMessage(input.trim()); input = "" } })
            )
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary).clickable { if (input.isNotBlank()) { vm.sendMessage(input.trim()); input = "" } }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Send, "Send", tint = Color(0xFF1A0F00), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: LiveChatMessage, isMine: Boolean) {
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val bubbleColor = when {
        msg.isHost -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        msg.isModerator -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        msg.isQuestion -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        isMine -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    val nameColor = when {
        msg.isHost -> MaterialTheme.colorScheme.primary
        msg.isModerator -> MaterialTheme.colorScheme.tertiary
        msg.isQuestion -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalAlignment = alignment) {
        if (!isMine) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(msg.displayName, style = MaterialTheme.typography.labelSmall, color = nameColor, fontWeight = FontWeight.SemiBold)
                if (msg.isHost) { Spacer(Modifier.width(4.dp)); Box(Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary).padding(horizontal = 4.dp, vertical = 1.dp)) { Text("HOST", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1A0F00)) } }
                if (msg.isModerator) { Spacer(Modifier.width(4.dp)); Box(Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.tertiary).padding(horizontal = 4.dp, vertical = 1.dp)) { Text("MOD", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1A0F00)) } }
            }
        }
        Box(Modifier.widthIn(max = 280.dp).clip(RoundedCornerShape(16.dp)).background(bubbleColor).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(msg.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun parseHosts(json: String): List<String> = try {
    JSONArray(json).let { arr -> (0 until arr.length()).map { arr.getString(it) } }
} catch (_: Exception) { emptyList() }
