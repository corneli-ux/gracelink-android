package com.gracelink.android.feature.events

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val DAY_OFFSETS = listOf(0, 1, 2, 3, 7, 14)
private val HOURS = (0..23).toList()
private val CATEGORIES = listOf("WORSHIP", "TEACHING", "YOUTH", "PRAYER", "FELLOWSHIP")

@Composable
fun EventCreateScreen(onBack: () -> Unit, onCreated: () -> Unit, vm: EventCreateViewModel = hiltViewModel()) {
    val me by vm.me.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("WORSHIP") }
    var dayOffset by remember { mutableStateOf(0) }
    var hour by remember { mutableStateOf(9) }
    var isOnline by remember { mutableStateOf(true) }
    var meetingLink by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Obsidian)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) }
            Text("Create Event", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }

        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            if (me == null) {
                Text("Set up your profile first.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
                return@Column
            }

            Field("Event title", title) { title = it }
            Spacer(Modifier.height(10.dp))
            Field("Description", description) { description = it }
            Spacer(Modifier.height(16.dp))

            Text("Category", style = MaterialTheme.typography.titleSmall, color = Gold400, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORIES) { c ->
                    Chip(c.lowercase().replaceFirstChar { it.uppercase() }, c == category) { category = c }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("When", style = MaterialTheme.typography.titleSmall, color = Gold400, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DAY_OFFSETS) { d ->
                    val label = when (d) { 0 -> "Today"; 1 -> "Tomorrow"; else -> "In $d days" }
                    Chip(label, d == dayOffset) { dayOffset = d }
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HOURS) { h ->
                    Chip(String.format("%02d:00", h), h == hour) { hour = h }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Online event", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Switch(checked = isOnline, onCheckedChange = { isOnline = it }, colors = SwitchDefaults.colors(checkedThumbColor = Gold400, checkedTrackColor = Gold400.copy(alpha = 0.4f)))
            }
            Spacer(Modifier.height(10.dp))
            if (isOnline) {
                Field("Meeting link (optional)", meetingLink) { meetingLink = it }
            } else {
                Field("Location", location) { location = it }
            }

            Spacer(Modifier.height(20.dp))
            GoldButton("Create Event", onClick = {
                if (title.isNotBlank()) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, dayOffset)
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, 0)
                    val start = cal.timeInMillis
                    val end = start + 60 * 60 * 1000
                    vm.createEvent(
                        title = title, description = description, startTime = start, endTime = end,
                        isOnline = isOnline, meetingLink = meetingLink.ifBlank { null },
                        location = location.ifBlank { null }, category = category, onDone = onCreated,
                    )
                }
            }, modifier = Modifier.fillMaxWidth())

            val previewCal = remember(dayOffset, hour) {
                Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset); set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, 0) }
            }
            Text(
                "Scheduled for ${SimpleDateFormat("EEE, MMM d \u2022 h:mm a", Locale.getDefault()).format(previewCal.time)}",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) Gold400 else Slate800).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurfaceVariant)
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
