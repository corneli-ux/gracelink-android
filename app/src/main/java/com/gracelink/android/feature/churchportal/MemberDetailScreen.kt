package com.gracelink.android.feature.churchportal

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.data.db.entity.AdminNoteEntity
import com.gracelink.android.data.db.entity.ChurchRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemberDetailScreen(memberId: String, onBack: () -> Unit, vm: MemberDetailViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val member = state.member
    var noteText by remember { mutableStateOf("") }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(memberId) { vm.load(memberId) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (member == null) {
            Text("Member not found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
            return@Column
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Text(member.displayName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(
                "Member since " + SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(member.joinedAt)),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!member.phone.isNullOrBlank() || !member.email.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    listOfNotNull(member.phone, member.email).joinToString(" \u00b7 "),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(20.dp))

            Text("ROLE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            RoleGrid(selected = member.role, onSelect = { vm.setRole(it) })

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            Text("ADMIN NOTES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Text("Only visible to church admins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))

            if (state.notes.isEmpty()) {
                Text("No notes yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            } else {
                state.notes.forEach { NoteRow(it) }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = noteText, onValueChange = { noteText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a note\u2026") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold500),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (noteText.isNotBlank()) { vm.addNote(noteText.trim()); noteText = "" } }),
                )
                Icon(
                    Icons.Rounded.Send, "Add note", tint = Gold500,
                    modifier = Modifier.size(20.dp).clickable { if (noteText.isNotBlank()) { vm.addNote(noteText.trim()); noteText = "" } },
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth().clickable { showRemoveConfirm = true }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Remove from church", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }

            if (showRemoveConfirm) {
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        "Confirm removal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { vm.removeMember(onBack) }.padding(end = 20.dp),
                    )
                    Text(
                        "Cancel", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { showRemoveConfirm = false },
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RoleGrid(selected: ChurchRole, onSelect: (ChurchRole) -> Unit) {
    Column {
        ChurchRole.values().toList().chunked(2).forEach { rowRoles ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowRoles.forEach { role ->
                    val isSelected = role == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Gold500 else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelect(role) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            role.name.replace('_', ' '),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color(0xFF1A0F00) else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (rowRoles.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NoteRow(note: AdminNoteEntity) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(note.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "${note.authorName} \u00b7 " + SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(note.createdAt)),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
