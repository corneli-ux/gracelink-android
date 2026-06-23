package com.gracelink.android.feature.fm

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.LiveBadge
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.FmScheduleEntity

@Composable
fun FmScreen(vm: FmViewModel = hiltViewModel()) {
    val schedule by vm.schedule.collectAsStateWithLifecycle()
    val today = remember { FmViewModel.today() }
    var selectedDay by remember { mutableStateOf(today) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Faith FM", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Text("24/7 Programming Schedule", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LiveBadge(text = "ON AIR")
        }

        // Day selector
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")) { day ->
                val selected = day == selectedDay
                val isToday = day == today
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Gold500 else Slate800)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        day,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Schedule list
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val daySchedule = schedule.filter { it.day == selectedDay }
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

            items(daySchedule, key = { it.id }) { slot ->
                val isNow = slot.startHour <= currentHour && currentHour < (slot.startHour + 2) && selectedDay == today
                ScheduleCard(slot, isNow)
            }
        }
    }
}

@Composable
private fun ScheduleCard(slot: FmScheduleEntity, isNow: Boolean) {
    val bg = if (isNow) Brush.horizontalGradient(listOf(Gold500.copy(alpha = 0.2f), Slate800))
             else Brush.verticalGradient(listOf(Slate800, Slate800))

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Time slot
            Column(Modifier.width(80.dp)) {
                Icon(Icons.Rounded.Schedule, null, tint = if (isNow) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text(if (slot.timeSlot.length >= 5) slot.timeSlot.substring(0, 5) else slot.timeSlot, style = MaterialTheme.typography.labelLarge, color = if (isNow) Gold500 else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(if (slot.timeSlot.length >= 13) slot.timeSlot.substring(8, 13) else "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Preacher info
            Column(Modifier.weight(1f)) {
                Text(slot.preacher, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(slot.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                CategoryChip(slot.category)
            }

            // Now playing indicator
            if (isNow) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(Gold500)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("NOW", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1A0F00), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: ContentCategory) {
    val color = when (category) {
        ContentCategory.WORSHIP -> Gold500
        ContentCategory.TEACHING -> Emerald500
        ContentCategory.REGIONAL -> Color(0xFF38BDF8)
        ContentCategory.DEBATES -> Color(0xFFA78BFA)
        ContentCategory.TESTIMONY -> Color(0xFFF43F5E)
        ContentCategory.YOUTH -> Color(0xFF6EE7B7)
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(category.name, style = MaterialTheme.typography.labelSmall, color = color)
    }
}