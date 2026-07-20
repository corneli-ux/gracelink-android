package com.gracelink.android.feature.announcements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracelink.android.core.theme.*
import com.gracelink.android.data.db.entity.AnnouncementPriority

@Composable
fun CreateAnnouncementScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    vm: AnnouncementsViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(AnnouncementPriority.NORMAL) }
    var isLoading by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Obsidian)) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                "New Announcement",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(20.dp))
            Text("Priority", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnnouncementPriority.entries.forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold500.copy(alpha = 0.2f),
                            selectedLabelColor = Gold500
                        )
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (title.isBlank() || body.isBlank()) return@Button
                    isLoading = true
                    vm.create(title.trim(), body.trim(), priority) {
                        isLoading = false
                        onCreated()
                    }
                },
                enabled = !isLoading && title.isNotBlank() && body.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = Obsidian, strokeWidth = 2.dp)
                } else {
                    Text("Publish", color = Obsidian, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold500,
    unfocusedBorderColor = Slate700,
    focusedLabelColor = Gold500,
    unfocusedLabelColor = TextSecondary,
    cursorColor = Gold500,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
