package com.gracelink.android.feature.audioconnect

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.components.LiveBadge
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900

@Composable
fun AudioConnectScreen(vm: AudioConnectViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.activeSpace != null) {
        ActiveSpaceView(
            space = state.activeSpace!!,
            isMicOn = state.isMicOn,
            isHandRaised = state.isHandRaised,
            onMicToggle = { vm.toggleMic() },
            onHandRaise = { vm.toggleHandRaise() },
            onLeave = { vm.leaveSpace() },
        )
    } else {
        SpacesListView(
            state = state,
            onJoin = { vm.joinSpace(it) },
            onCreate = { vm.showCreateDialog(true) },
        )
    }

    if (state.isCreating) {
        CreateSpaceDialog(
            onCreate = { title, topic -> vm.createSpace(title, topic, "You") },
            onDismiss = { vm.showCreateDialog(false) },
        )
    }
}

@Composable
private fun SpacesListView(
    state: AudioConnectState,
    onJoin: (AudioSpace) -> Unit,
    onCreate: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Audio Connect", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Text("Live audio spaces — join the conversation", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Gold500)
                    .clickable(onClick = onCreate),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, "Create space", tint = Color(0xFF1A0F00), modifier = Modifier.size(24.dp))
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.spaces, key = { it.id }) { space ->
                SpaceCard(space) { onJoin(space) }
            }
        }
    }
}

@Composable
private fun SpaceCard(space: AudioSpace, onJoin: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Gold500.copy(alpha = 0.15f), Slate800)))
            .clickable(onClick = onJoin)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveBadge()
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.People, null, tint = Gold500, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${space.participantCount} listening", style = MaterialTheme.typography.labelMedium, color = Gold500)
                Spacer(Modifier.weight(1f))
                Text(formatDuration(System.currentTimeMillis() - space.startedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Text(space.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text("Hosted by ${space.hostName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(space.topic, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gold500)
                    .clickable(onClick = onJoin)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Join Space", color = Color(0xFF1A0F00), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ActiveSpaceView(
    space: AudioSpace,
    isMicOn: Boolean,
    isHandRaised: Boolean,
    onMicToggle: () -> Unit,
    onHandRaise: () -> Unit,
    onLeave: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background)))
            .statusBarsPadding()
    ) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Slate800).clickable(onClick = onLeave), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ArrowBack, "Leave", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(12.dp))
            LiveBadge()
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.People, null, tint = Gold500, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("${space.participantCount}", style = MaterialTheme.typography.labelLarge, color = Gold500)
        }

        // Space info
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text(space.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text("Hosted by ${space.hostName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(space.topic, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(Modifier.weight(1f))

        // Participant circle (visual)
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Gold500.copy(alpha = 0.3f), Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.People, null, tint = Gold500, modifier = Modifier.size(48.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        // Controls
        Row(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Slate900)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mic toggle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isMicOn) Gold500 else Slate800)
                        .clickable(onClick = onMicToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isMicOn) Icons.Rounded.Mic else Icons.Rounded.MicOff, "Mic", tint = if (isMicOn) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(if (isMicOn) "Mic On" else "Mic Off", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Hand raise
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isHandRaised) Emerald500 else Slate800)
                        .clickable(onClick = onHandRaise),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.PanTool, "Raise hand", tint = if (isHandRaised) Color(0xFF00211A) else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(if (isHandRaised) "Hand Raised" else "Raise Hand", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Leave
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable(onClick = onLeave),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, "Leave", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text("Leave", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun CreateSpaceDialog(onCreate: (String, String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onDismiss)) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Slate900)
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Column {
                Text("Start Audio Space", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("Create a live audio room for discussion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))

                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    TextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Space title (e.g. Bible Study)", color = MaterialTheme.colorScheme.onSurfaceVariant) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold500), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    TextField(value = topic, onValueChange = { topic = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Topic (e.g. Romans 8)", color = MaterialTheme.colorScheme.onSurfaceVariant) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold500), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { if (title.isNotBlank()) onCreate(title, topic) }))
                }
                Spacer(Modifier.height(20.dp))
                GoldButton("Start Space", onClick = { if (title.isNotBlank()) onCreate(title, topic) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).clickable(onClick = onDismiss).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val mins = ms / 60000
    return if (mins < 60) "${mins}m" else "${mins / 60}h ${mins % 60}m"
}
