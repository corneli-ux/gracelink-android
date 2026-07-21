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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.data.db.entity.RsvpStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventRsvpScreen(
    eventId: String,
    onBack: () -> Unit,
    onRequireSignIn: () -> Unit = {},
    vm: EventRsvpViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val event = state.event

    LaunchedEffect(eventId) { vm.load(eventId) }

    Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (event == null) {
            Text("Event not found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
            return@Column
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text(event.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            Text(
                SimpleDateFormat("EEEE, MMM d \u00b7 h:mm a", Locale.getDefault()).format(Date(event.startTime)) +
                    if (event.isOnline) " \u00b7 Online" else event.location?.let { " \u00b7 $it" }.orEmpty(),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (event.description.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(event.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(Modifier.height(24.dp))
            Text("Will you be attending?", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RsvpOption("Going", RsvpStatus.GOING, state.myStatus) { if (state.myUid.isBlank()) onRequireSignIn() else vm.setRsvp(RsvpStatus.GOING) }
                RsvpOption("Maybe", RsvpStatus.MAYBE, state.myStatus) { if (state.myUid.isBlank()) onRequireSignIn() else vm.setRsvp(RsvpStatus.MAYBE) }
                RsvpOption("Can't Go", RsvpStatus.NOT_GOING, state.myStatus) { if (state.myUid.isBlank()) onRequireSignIn() else vm.setRsvp(RsvpStatus.NOT_GOING) }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "${state.goingCount} going \u00b7 ${state.maybeCount} maybe",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RsvpOption(label: String, value: RsvpStatus, selected: RsvpStatus?, onClick: () -> Unit) {
    val isSelected = value == selected
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (isSelected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface)
    }
}
