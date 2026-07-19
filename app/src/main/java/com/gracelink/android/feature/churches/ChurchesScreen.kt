package com.gracelink.android.feature.churches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.data.db.entity.ChurchEntity
import com.gracelink.android.data.db.entity.VerificationStatus

private enum class ChurchFilter(val label: String) { ALL("All"), VERIFIED("Verified"), MINE("My Church") }

@Composable
fun ChurchesScreen(
    onChurchClick: (String) -> Unit = {},
    onRequireSignIn: () -> Unit = {},
    vm: ChurchesViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChurchFilter.ALL) }

    val filtered = state.churches
        .filter { it.name.contains(query, ignoreCase = true) || it.location.contains(query, ignoreCase = true) }
        .filter {
            when (filter) {
                ChurchFilter.ALL -> true
                ChurchFilter.VERIFIED -> it.verificationStatus == VerificationStatus.VERIFIED
                ChurchFilter.MINE -> it.id == state.myChurchId
            }
        }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Churches", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Text("Find your community & join", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Gold400).clickable {
                    if (state.isGuest) onRequireSignIn() else showCreate = true
                },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, "Create church", tint = Color(0xFF1A1408), modifier = Modifier.size(22.dp))
            }
        }

        // Search
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Slate800)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search by name or location", style = MaterialTheme.typography.bodyMedium) },
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400),
                singleLine = true,
            )
        }
        Spacer(Modifier.height(10.dp))

        // Filter chips
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChurchFilter.values().forEach { f ->
                val selected = filter == f
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) Gold400 else Slate800)
                        .clickable { filter = f }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(f.label, style = MaterialTheme.typography.labelMedium, color = if (selected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (filtered.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(40.dp))
                Icon(Icons.Rounded.Church, null, tint = Gold400.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text("No churches match", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { church ->
                    ChurchCard(
                        church, state.myChurchId == church.id,
                        onJoin = { if (state.isGuest) onRequireSignIn() else vm.joinChurch(church) },
                        onClick = { onChurchClick(church.id) },
                    )
                }
            }
        }
    }

    if (showCreate) {
        CreateChurchDialog(onCreate = { n, p, l, b, e -> vm.createChurch(n, p, l, b, e); showCreate = false }, onDismiss = { showCreate = false })
    }
}

@Composable
private fun ChurchCard(church: ChurchEntity, isMember: Boolean, onJoin: () -> Unit, onClick: () -> Unit) {
    val isVerified = church.verificationStatus == VerificationStatus.VERIFIED
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Brush.horizontalGradient(listOf(Gold400.copy(alpha = 0.1f), Slate800))).clickable(onClick = onClick).padding(16.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Church, null, tint = Gold400, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(church.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                        if (isVerified) { Spacer(Modifier.width(6.dp)); Icon(Icons.Rounded.CheckCircle, "Verified", tint = Emerald500, modifier = Modifier.size(16.dp)) }
                    }
                    Text(church.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Pastor: ${church.pastorName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Belief: ${church.beliefSystem.displayName}", style = MaterialTheme.typography.bodySmall, color = Gold400)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.People, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${church.memberCount} members", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                if (isMember) {
                    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Emerald500.copy(alpha = 0.2f)).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text("Member", style = MaterialTheme.typography.labelMedium, color = Emerald500, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Gold400).clickable(onClick = onJoin).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text("Join", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1A1408), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (church.verificationStatus == VerificationStatus.PENDING) {
                Spacer(Modifier.height(6.dp))
                Text("Verification pending \u2014 certificate & photos under review", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CreateChurchDialog(onCreate: (String, String, String, BeliefSystem, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var pastor by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var belief by remember { mutableStateOf(BeliefSystem.NONDENOMINATIONAL) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onDismiss)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(24.dp).clickable(enabled = false) {}) {
            Column {
                Text("Register Your Church", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("Your church will be verified before going public", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Field("Church Name", name) { name = it }
                Spacer(Modifier.height(10.dp))
                Field("Pastor's Name", pastor) { pastor = it }
                Spacer(Modifier.height(10.dp))
                Field("Location", location) { location = it }
                Spacer(Modifier.height(10.dp))
                Field("Email", email) { email = it }
                Spacer(Modifier.height(12.dp))
                Text("Belief System", style = MaterialTheme.typography.labelMedium, color = Gold400, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                BeliefSystem.values().take(6).forEach { b ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (b == belief) Gold400.copy(alpha = 0.15f) else Slate800).clickable { belief = b }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = b == belief, onClick = { belief = b }, colors = RadioButtonDefaults.colors(selectedColor = Gold400))
                        Spacer(Modifier.width(8.dp))
                        Text(b.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("After creation, upload your church certificate & photos for verification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                GoldButton("Register Church", onClick = { if (name.isNotBlank() && pastor.isNotBlank()) onCreate(name, pastor, location, belief, email) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).clickable(onClick = onDismiss).padding(vertical = 14.dp), contentAlignment = Alignment.Center) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text(label) }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
}
