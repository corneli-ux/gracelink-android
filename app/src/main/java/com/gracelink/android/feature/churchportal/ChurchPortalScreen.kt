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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SupervisorAccount
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GlassCard
import com.gracelink.android.core.components.VerifiedBadge
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.GoldGradient
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.ChurchMemberEntity
import com.gracelink.android.data.db.entity.VerificationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GraceLink church leadership dashboard -- glass-card design pattern:
 * translucent GlassCard containers replace flat hairline dividers,
 * gold gradient header for church identity, gold accents on stats
 * and interactive elements. Backed by a real ChurchEntity linked
 * via ownerUserId.
 */
@Composable
fun ChurchPortalScreen(
    onBack: () -> Unit,
    onScheduleRadio: () -> Unit,
    onStartSpace: () -> Unit,
    onOpenPodcasts: () -> Unit,
    onWriteArticle: () -> Unit,
    onCreateEvent: () -> Unit,
    onEditProfile: () -> Unit,
    onViewMembers: () -> Unit,
    onOpenAnnouncements: () -> Unit,
    onOpenGroups: () -> Unit,
    onOpenLeadership: () -> Unit,
    onOpenMinistries: () -> Unit,
    onOpenEventRsvp: (String) -> Unit,
    onOpenServiceTimes: () -> Unit,
    onOpenModerationLog: () -> Unit,
    onOpenInsights: () -> Unit,
    vm: ChurchPortalViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val church = state.church

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.weight(1f))
            if (church != null) {
                IconButton(
                    onClick = onEditProfile,
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = Gold400.copy(alpha = 0.15f)),
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit profile", tint = Gold400)
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            if (church == null) {
                Text(
                    "Set up your profile as a Church to unlock this GraceLink dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 40.dp),
                )
                return@Column
            }

            // ── Identity -- gold gradient header inside GlassCard ──────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(GoldGradient))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                church.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = androidx.compose.ui.graphics.Color(0xFF1A0F00), // dark text on gold
                            )
                            if (church.verificationStatus == VerificationStatus.VERIFIED) {
                                Spacer(Modifier.width(6.dp))
                                VerifiedBadge(size = 18.dp)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            church.verificationStatus.name.lowercase().replaceFirstChar { it.uppercase() } + " \u00b7 ${church.memberCount} members",
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color(0xFF3D2B10),
                            modifier = Modifier.clickable(onClick = onViewMembers),
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Stat row -- gold-accented values inside GlassCard ──────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Stat("${state.pendingMembers.size}", "Pending")
                    Stat("${state.articleCount}", "Articles")
                    Stat("${state.podcastCount}", "Podcasts")
                    Stat("${state.upcomingEvents.size}", "Events")
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Pending membership requests ──────────────────────────────────
            if (state.pendingMembers.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel("Membership requests")
                        state.pendingMembers.forEach { member ->
                            PendingMemberRow(member, onApprove = { vm.approveMember(member.id) }, onReject = { vm.rejectMember(member.id) })
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Incoming collaboration proposals ──────────────────────────────
            // Other churches/pastors asking to partner on events, debates, or
            // discussions. Distinct from membership: churches don't join each other.
            if (state.pendingCollaborations.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel("Collaboration requests")
                        state.pendingCollaborations.forEach { req ->
                            CollaborationRequestRow(req, onAccept = { vm.respondToCollaboration(req.id, true) }, onDecline = { vm.respondToCollaboration(req.id, false) })
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Upcoming events ──────────────────────────────────────────────
            // Events you created now have a dedicated display section.
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    SectionLabel("Upcoming events")
                    if (state.upcomingEvents.isEmpty()) {
                        Text("No events yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    } else {
                        state.upcomingEvents.forEach { evt -> EventRow(evt) { onOpenEventRsvp(evt.id) } }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Manage -- portal actions inside GlassCard ──────────────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionLabel("Manage")
                    PortalAction("Insights", Icons.Rounded.Insights, onOpenInsights)
                    PortalAction("Announcements", Icons.Rounded.Campaign, onOpenAnnouncements)
                    PortalAction("Groups", Icons.Rounded.Groups, onOpenGroups)
                    PortalAction("Leadership Team", Icons.Rounded.SupervisorAccount, onOpenLeadership)
                    PortalAction("Ministries", Icons.Rounded.VolunteerActivism, onOpenMinistries)
                    PortalAction("Service Times", Icons.Rounded.Schedule, onOpenServiceTimes)
                    PortalAction("Moderation Log", Icons.Rounded.Shield, onOpenModerationLog)
                    PortalAction("Radio Schedule", Icons.Rounded.Radio, onScheduleRadio)
                    PortalAction("Start Live Space", Icons.Rounded.Headphones, onStartSpace)
                    PortalAction("Podcasts", Icons.Rounded.Podcasts, onOpenPodcasts)
                    PortalAction("Write Article / Post", Icons.Rounded.Article, onWriteArticle)
                    PortalAction("Create Event", Icons.Rounded.CalendarMonth, onCreateEvent)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Gold400)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Gold400,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun PendingMemberRow(member: ChurchMemberEntity, onApprove: () -> Unit, onReject: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(member.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.Check, "Approve", tint = Gold400, modifier = Modifier.size(22.dp).clickable(onClick = onApprove))
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Rounded.Close, "Reject", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp).clickable(onClick = onReject))
    }
}

@Composable
private fun CollaborationRequestRow(req: com.gracelink.android.data.db.entity.CollaborationRequestEntity, onAccept: () -> Unit, onDecline: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(req.fromName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(req.fromType.name.lowercase().replaceFirstChar { it.uppercase() } + " wants to collaborate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.Check, "Accept", tint = Gold400, modifier = Modifier.size(22.dp).clickable(onClick = onAccept))
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Rounded.Close, "Decline", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp).clickable(onClick = onDecline))
        }
        if (req.message.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(req.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EventRow(event: ChurchEventEntity, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp)) {
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(10.dp), ambientColor = Gold400.copy(alpha = 0.10f))
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Gold400, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
    }
}
