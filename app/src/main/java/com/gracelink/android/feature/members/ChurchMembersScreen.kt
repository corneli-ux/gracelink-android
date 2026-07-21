package com.gracelink.android.feature.members

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.theme.*
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.ChurchRole
import com.gracelink.android.data.db.entity.MemberStatus

@Composable
fun ChurchMembersScreen(
    onBack: () -> Unit,
    onMemberClick: (String) -> Unit,
    vm: ChurchMembersViewModel = hiltViewModel()
) {
    val members by vm.members.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    val filtered = remember(members, query) {
        if (query.isBlank()) members
        else members.filter {
            it.displayName.contains(query, ignoreCase = true) ||
            it.role.name.contains(query, ignoreCase = true)
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().background(Obsidian)) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                "${members.size} Members",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search members...") },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold500,
                unfocusedBorderColor = Slate700,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Gold500
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
            items(filtered, key = { it.id }) { member ->
                MemberRow(member) { onMemberClick(member.id) }
                HorizontalDivider(color = Slate700, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun MemberRow(member: ChurchMemberEntity, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Slate800),
            contentAlignment = Alignment.Center
        ) {
            Text(
                member.displayName.take(1).uppercase(),
                color = Gold500,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(member.displayName, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(
                member.role.name.replace('_', ' ') +
                    if (member.status != MemberStatus.APPROVED) " · ${member.status.name}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = when (member.status) {
                    MemberStatus.PENDING -> Gold500
                    MemberStatus.REJECTED, MemberStatus.REMOVED -> LiveRed
                    else -> TextSecondary
                }
            )
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(14.dp))
    }
}
