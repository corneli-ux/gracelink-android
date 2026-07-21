package com.gracelink.android.feature.announcements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.theme.*
import com.gracelink.android.data.db.entity.AnnouncementEntity
import com.gracelink.android.data.db.entity.AnnouncementPriority
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnnouncementsScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    vm: AnnouncementsViewModel = hiltViewModel()
) {
    val announcements by vm.announcements.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().statusBarsPadding().background(Obsidian)) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                "Announcements",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCreate) {
                Icon(Icons.Rounded.Add, "Create", tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (announcements.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No announcements yet", color = TextSecondary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(announcements, key = { it.id }) { item ->
                    AnnouncementCard(item)
                }
            }
        }
    }
}

@Composable
private fun AnnouncementCard(item: AnnouncementEntity) {
    val priorityColor = when (item.priority) {
        AnnouncementPriority.URGENT -> LiveRed
        AnnouncementPriority.HIGH -> MaterialTheme.colorScheme.primary
        else -> TextSecondary
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(Slate900, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.isPinned) {
                Icon(Icons.Rounded.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                item.priority.name,
                style = MaterialTheme.typography.labelSmall,
                color = priorityColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(item.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            item.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            item.body,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 4
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "— ${item.authorName}",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
