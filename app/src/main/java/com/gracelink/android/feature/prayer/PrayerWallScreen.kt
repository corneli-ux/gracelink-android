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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.designsystem.theme.Emerald500
import com.gracelink.android.core.designsystem.theme.Gold500
import com.gracelink.android.core.designsystem.theme.GraceGradients
import com.gracelink.android.core.designsystem.theme.Slate800
import com.gracelink.android.core.designsystem.theme.Slate900
import com.gracelink.android.data.model.Encouragement
import com.gracelink.android.data.model.PrayerRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Prayer Wall — spec §4.6 + §3 Prayer Wall.
 * Tabs (All / My / Answered). Cards with request text, timestamp, 'I prayed'
 * button + count, 'Encourage' option.
 */
@Composable
fun PrayerWallScreen(
    viewModel: PrayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Prayer Wall",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Stand in the gap for one another",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Gold500)
                        .clickable { viewModel.showSubmitSheet(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New prayer", tint = Color(0xFF1A1206), modifier = Modifier.size(24.dp))
                }
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PrayerTab.values().forEach { tab ->
                    val selected = state.activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Gold500 else Color.Transparent)
                            .clickable { viewModel.setTab(tab) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color(0xFF1A1206) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.prayers, key = { it.id }) { prayer ->
                    PrayerCard(
                        prayer = prayer,
                        onPray = { viewModel.togglePrayed(prayer.id) },
                        onAnswered = { viewModel.markAnswered(prayer.id) },
                        onEncourage = { text -> viewModel.addEncouragement(prayer.id, text) },
                    )
                }
            }
        }

        // Submit-prayer bottom sheet
        AnimatedVisibility(visible = state.showSubmitSheet) {
            SubmitPrayerSheet(
                onSubmit = { text, anon -> viewModel.submitPrayer(text, anon) },
                onDismiss = { viewModel.showSubmitSheet(false) }
            )
        }
    }
}

@Composable
private fun PrayerCard(
    prayer: PrayerRequest,
    onPray: () -> Unit,
    onAnswered: () -> Unit,
    onEncourage: (String) -> Unit,
) {
    var showEncourageInput by remember { mutableStateOf(false) }
    var encouragementText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (prayer.isAnswered) GraceGradients.prayerCard()
                else Brush.verticalGradient(listOf(Slate800, Slate800))
            )
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (prayer.isAnswered) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "ANSWERED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald500,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = prayer.displayName ?: "Anonymous",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "• ${formatTime(prayer.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = prayer.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Encouragements
            if (prayer.encouragements.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    prayer.encouragements.take(2).forEach { enc ->
                        EncouragementRow(enc)
                    }
                    if (prayer.encouragements.size > 2) {
                        Text(
                            text = "+${prayer.encouragements.size - 2} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // I Prayed button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (prayer.userPrayedThis) Emerald500.copy(alpha = 0.25f)
                            else Slate900.copy(alpha = 0.7f)
                        )
                        .clickable(onClick = onPray)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (prayer.userPrayedThis) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (prayer.userPrayedThis) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "I Prayed • ${prayer.prayedCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (prayer.userPrayedThis) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (!prayer.isAnswered) {
                    Text(
                        text = "Encourage",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showEncourageInput = !showEncourageInput }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showEncourageInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate900)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = encouragementText,
                        onValueChange = { encouragementText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Send an encouragement…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Gold500,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (encouragementText.isNotBlank()) {
                                onEncourage(encouragementText.trim())
                                encouragementText = ""
                                showEncourageInput = false
                            }
                        })
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Gold500)
                            .clickable {
                                if (encouragementText.isNotBlank()) {
                                    onEncourage(encouragementText.trim())
                                    encouragementText = ""
                                    showEncourageInput = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color(0xFF1A1206), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EncouragementRow(enc: Encouragement) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Emerald500.copy(alpha = 0.6f))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${enc.displayName ?: "Anonymous"}: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = enc.text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SubmitPrayerSheet(
    onSubmit: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Slate900)
                .padding(20.dp)
                .navigationBarsPadding()
                .imePadding()
                .clickable(enabled = false) {}
        ) {
            Column {
                Text(
                    "Share a Prayer",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "All prayers go through moderation before appearing publicly. Be honest and respectful.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate800)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Write your prayer…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Gold500,
                        ),
                        minLines = 3,
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Post anonymously",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = anonymous,
                        onCheckedChange = { anonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Gold500,
                            checkedTrackColor = Gold500.copy(alpha = 0.4f),
                            uncheckedTrackColor = Slate800,
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate800)
                            .clickable(onClick = onDismiss)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Gold500)
                            .clickable { if (text.isNotBlank()) onSubmit(text.trim(), anonymous) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Submit Prayer", color = Color(0xFF1A1206), fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

private fun formatTime(epoch: Long): String {
    val diffMs = System.currentTimeMillis() - epoch
    val hours = diffMs / 3_600_000
    val days = hours / 24
    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        else -> "${diffMs / 60_000}m ago"
    }
}

