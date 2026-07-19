package com.gracelink.android.feature.fm

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.ContentCategory
import com.gracelink.android.data.db.entity.FmScheduleEntity

@Composable
fun FmScreen(vm: FmViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("GraceLink Radio", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Text("24/7 Live Broadcast", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LiveBadge(text = "ON AIR")
        }

        // -- Live Player Hero --------------------------------------------------
        state.currentSlot?.let { slot ->
            LivePlayerHero(slot, state.isPlaying, state.isLoading, vm::togglePlay)
            if (state.errorMessage != null) {
                Text(
                    "Couldn't connect to the stream: ${state.errorMessage}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        // Day selector
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")) { day ->
                val selected = day == state.selectedDay
                Box(Modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) Gold500 else Slate800).clickable { vm.selectDay(day) }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(day, style = MaterialTheme.typography.labelLarge, color = if (selected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Schedule list
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val daySchedule = state.schedule.filter { it.day == state.selectedDay }
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val today = FmViewModel.today()

            if (daySchedule.isEmpty()) {
                item {
                    Text("No schedule for this day yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
                }
            }

            items(daySchedule, key = { it.id }) { slot ->
                val isNow = slot.startHour <= currentHour && currentHour < (slot.startHour + 2) && state.selectedDay == today
                ScheduleCard(slot, isNow)
            }
        }
    }
}

@Composable
private fun LivePlayerHero(slot: FmScheduleEntity, isPlaying: Boolean, isLoading: Boolean, onToggle: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "fm-eq")
    val bar1 by transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "b1")
    val bar2 by transition.animateFloat(1f, 0.35f, infiniteRepeatable(tween(560), RepeatMode.Reverse), label = "b2")
    val bar3 by transition.animateFloat(0.5f, 1f, infiniteRepeatable(tween(340), RepeatMode.Reverse), label = "b3")

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Gold500.copy(alpha = 0.28f), Slate900)))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Gold500)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1A0F00),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play/Pause", tint = Color(0xFF1A0F00), modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = Gold500, fontWeight = FontWeight.Bold)
                    if (isPlaying) {
                        Spacer(Modifier.width(8.dp))
                        EqualizerBars(bar1, bar2, bar3)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(slot.preacher, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(slot.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun EqualizerBars(a: Float, b: Float, c: Float) {
    Row(verticalAlignment = Alignment.Bottom) {
        listOf(a, b, c).forEach { h ->
            Box(
                Modifier
                    .padding(horizontal = 1.dp)
                    .width(3.dp)
                    .height((h * 10).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Gold500)
            )
        }
    }
}

@Composable
private fun ScheduleCard(slot: FmScheduleEntity, isNow: Boolean) {
    val bg = if (isNow) Brush.horizontalGradient(listOf(Gold400.copy(alpha = 0.2f), Slate800)) else Brush.verticalGradient(listOf(Slate800, Slate800))
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(70.dp)) {
                Icon(Icons.Rounded.Schedule, null, tint = if (isNow) Gold400 else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text(if (slot.timeSlot.length >= 5) slot.timeSlot.substring(0, 5) else slot.timeSlot, style = MaterialTheme.typography.labelLarge, color = if (isNow) Gold400 else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(if (slot.timeSlot.length >= 13) slot.timeSlot.substring(8, 13) else "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.weight(1f)) {
                Text(slot.preacher, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(slot.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                CategoryChip(slot.category)
            }
            if (isNow) {
                Box(Modifier.clip(CircleShape).background(Gold400).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text("NOW", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1A1408), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: ContentCategory) {
    val color = when (category) {
        ContentCategory.WORSHIP -> Gold400
        ContentCategory.TEACHING -> Emerald500
        ContentCategory.REGIONAL -> Color(0xFF5AB8E0)
        ContentCategory.DEBATES -> Color(0xFFB89CD9)
        ContentCategory.TESTIMONY -> Color(0xFFF43F5E)
        ContentCategory.YOUTH -> Color(0xFF7BD9A8)
    }
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(category.name, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
