package com.gracelink.android.feature.radiobooking

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Schedule
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
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.data.db.entity.FmScheduleEntity

private val DAYS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

@Composable
fun RadioBookingScreen(onBack: () -> Unit, vm: RadioBookingViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.message) {
        if (state.message != null) {
            kotlinx.coroutines.delay(2000)
            vm.clearMessage()
        }
    }

    Column(Modifier.fillMaxSize().background(Obsidian)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Book a Radio Slot", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            "Open slots are first-come, first-served \u2014 booking is live.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp),
        )

        if (state.message != null) {
            Text(state.message ?: "", style = MaterialTheme.typography.labelMedium, color = Gold500, modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp))
        }

        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DAYS) { day ->
                val selected = day == state.selectedDay
                Box(Modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) Gold400 else Slate800).clickable { vm.selectDay(day) }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(day, style = MaterialTheme.typography.labelLarge, color = if (selected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.daySlots, key = { it.id }) { slot ->
                val isMine = slot.bookedByUid != null && slot.bookedByUid == state.me?.uid
                val isTaken = slot.bookedByUid != null
                SlotRow(
                    slot = slot,
                    isMine = isMine,
                    isTaken = isTaken,
                    onBook = { vm.bookSlot(slot.id) },
                    onCancel = { vm.cancelBooking(slot.id) },
                )
            }
        }
    }
}

@Composable
private fun SlotRow(slot: FmScheduleEntity, isMine: Boolean, isTaken: Boolean, onBook: () -> Unit, onCancel: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(70.dp)) {
            Icon(Icons.Rounded.Schedule, null, tint = if (isMine) Gold400 else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(if (slot.timeSlot.length >= 5) slot.timeSlot.substring(0, 5) else slot.timeSlot, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
        Column(Modifier.weight(1f)) {
            Text(
                if (isTaken) slot.bookedByName ?: slot.preacher else "Open slot",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(slot.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        when {
            isMine -> Box(Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.error).clickable(onClick = onCancel).padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("Cancel", style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
            isTaken -> Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Slate800).padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("Booked", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Gold500).clickable(onClick = onBook).padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("Book", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1A0F00), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
