package com.gracelink.android.feature.churchportal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate850
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.VerificationStatus

/**
 * Church leadership dashboard, backed by a real ChurchEntity linked via
 * ownerUserId. Shows the actual verification status and member counts
 * (previously hardcoded to zero) and lets the church approve or reject
 * pending membership requests right here.
 */
@Composable
fun ChurchPortalScreen(
    onBack: () -> Unit,
    onScheduleRadio: () -> Unit,
    onStartSpace: () -> Unit,
    onOpenPodcasts: () -> Unit,
    onWriteArticle: () -> Unit,
    onCreateEvent: () -> Unit,
    vm: ChurchPortalViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val church = state.church

    Column(modifier = Modifier.fillMaxSize().background(Obsidian)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Church Portal", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            if (church == null) {
                Text(
                    "Set up your profile as a Church to unlock this dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 40.dp),
                )
                return@Column
            }

            // Status card
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF1A2A1A), Slate850)))
                    .border(1.dp, Emerald500.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Verified, null, tint = if (church.verificationStatus == VerificationStatus.VERIFIED) Emerald500 else Gold400, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(church.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            "Verification: ${church.verificationStatus.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Members: ${church.memberCount} \u2022 Pending: ${state.pendingMembers.size} \u2022 Articles: ${state.articleCount} \u2022 Podcasts: ${state.podcastCount}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Pending membership requests
            Text("MEMBERSHIP REQUESTS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            if (state.pendingMembers.isEmpty()) {
                Text("No pending requests", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            } else {
                state.pendingMembers.forEach { member ->
                    PendingMemberRow(member, onApprove = { vm.approveMember(member.id) }, onReject = { vm.rejectMember(member.id) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("MANAGE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(12.dp))

            PortalAction("Radio Schedule", "Book a live slot on Faith Link Radio", Icons.Rounded.Radio, Gold400, onScheduleRadio)
            Spacer(modifier = Modifier.height(10.dp))
            PortalAction("Start Live Space", "Host a live audio room for your congregation", Icons.Rounded.Headphones, Emerald500, onStartSpace)
            Spacer(modifier = Modifier.height(10.dp))
            PortalAction("Podcasts", "Publish a new series or episode", Icons.Rounded.Podcasts, Color(0xFF5AB8E0), onOpenPodcasts)
            Spacer(modifier = Modifier.height(10.dp))
            PortalAction("Write Article / Post", "Share teaching or a quick update", Icons.Rounded.Article, Gold500, onWriteArticle)
            Spacer(modifier = Modifier.height(10.dp))
            PortalAction("Create Event", "Schedule a service, class, or gathering", Icons.Rounded.CalendarMonth, Color(0xFFB89CD9), onCreateEvent)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PendingMemberRow(member: ChurchMemberEntity, onApprove: () -> Unit, onReject: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate850).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Groups, null, tint = Gold400, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(member.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Emerald500).clickable(onClick = onApprove).padding(8.dp)) {
            Icon(Icons.Rounded.Check, "Approve", tint = Color(0xFF00211A), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.error).clickable(onClick = onReject).padding(8.dp)) {
            Icon(Icons.Rounded.Close, "Reject", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PortalAction(title: String, subtitle: String, icon: ImageVector, accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Slate850).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}
