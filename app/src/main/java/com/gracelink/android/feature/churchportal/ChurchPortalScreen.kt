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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.VerificationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Church leadership dashboard -- minimalist: flat background, hairline
 * dividers between sections instead of colored cards, text carries the
 * hierarchy. Backed by a real ChurchEntity linked via ownerUserId.
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            if (church == null) {
                Text(
                    "Set up your profile as a Church to unlock this dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 40.dp),
                )
                return@Column
            }

            // Identity -- plain text, no card
            Text(church.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(
                church.verificationStatus.name.lowercase().replaceFirstChar { it.uppercase() } + " \u00b7 ${church.memberCount} members",
                style = MaterialTheme.typography.bodyMedium,
                color = if (church.verificationStatus == VerificationStatus.VERIFIED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))

            // Stat row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("${state.pendingMembers.size}", "Pending")
                Stat("${state.articleCount}", "Articles")
                Stat("${state.podcastCount}", "Podcasts")
                Stat("${state.upcomingEvents.size}", "Events")
            }
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Pending membership requests
            if (state.pendingMembers.isNotEmpty()) {
                SectionLabel("Membership requests")
                state.pendingMembers.forEach { member ->
                    PendingMemberRow(member, onApprove = { vm.approveMember(member.id) }, onReject = { vm.rejectMember(member.id) })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            // Upcoming events -- this is what was missing before: events
            // you created had nowhere to actually show up
            SectionLabel("Upcoming events")
            if (state.upcomingEvents.isEmpty()) {
                Text("No events yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
            } else {
                state.upcomingEvents.forEach { EventRow(it) }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SectionLabel("Manage")
            PortalAction("Radio Schedule", Icons.Rounded.Radio, onScheduleRadio)
            PortalAction("Start Live Space", Icons.Rounded.Headphones, onStartSpace)
            PortalAction("Podcasts", Icons.Rounded.Podcasts, onOpenPodcasts)
            PortalAction("Write Article / Post", Icons.Rounded.Article, onWriteArticle)
            PortalAction("Create Event", Icons.Rounded.CalendarMonth, onCreateEvent)

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 16.dp),
    )
}

@Composable
private fun PendingMemberRow(member: ChurchMemberEntity, onApprove: () -> Unit, onReject: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(member.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.Check, "Approve", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp).clickable(onClick = onApprove))
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Rounded.Close, "Reject", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp).clickable(onClick = onReject))
    }
}

@Composable
private fun EventRow(event: ChurchEventEntity) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(event.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            SimpleDateFormat("EEE, MMM d \u00b7 h:mm a", Locale.getDefault()).format(Date(event.startTime)) +
                if (event.isOnline) " \u00b7 Online" else event.location?.let { " \u00b7 $it" }.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PortalAction(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
    }
}
