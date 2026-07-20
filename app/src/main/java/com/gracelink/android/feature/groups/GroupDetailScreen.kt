package com.gracelink.android.feature.groups

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GhostButton
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.data.db.entity.GroupMemberEntity

@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onRequireSignIn: () -> Unit = {},
    vm: GroupDetailViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val group = state.group

    LaunchedEffect(groupId) { vm.load(groupId) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (group == null) {
            Text("Group not found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
            return@Column
        }

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 24.dp)) {
            item {
                Text(group.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(group.type.name.replace('_', ' ') + " \u00b7 ${state.members.size} members", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text(group.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))

                Row {
                    if (state.isMember) {
                        GoldButton("Open Chat", icon = Icons.Rounded.ChatBubbleOutline, onClick = onOpenChat, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(10.dp))
                        GhostButton("Leave", onClick = { vm.leaveGroup() }, modifier = Modifier.weight(1f))
                    } else {
                        GoldButton("Join Group", onClick = {
                            if (state.myUid.isBlank()) onRequireSignIn() else vm.joinGroup()
                        }, modifier = Modifier.fillMaxWidth())
                    }
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Text("MEMBERS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }

            items(state.members, key = { it.id }) { member ->
                MemberChip(member)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MemberChip(member: GroupMemberEntity) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Text(member.displayName.take(1).uppercase(), color = Gold500, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(member.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(member.role.name.replace('_', ' '), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
