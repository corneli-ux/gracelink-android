package com.gracelink.android.feature.churches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GlassCard
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.components.VerifiedBadge
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.GoldGradient
import com.gracelink.android.core.theme.GlassMedium
import com.gracelink.android.core.theme.GlassBorder
import com.gracelink.android.core.theme.Slate850
import com.gracelink.android.data.db.entity.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ChurchTab(val label: String) { ABOUT("About"), ARTICLES("Articles"), PODCASTS("Podcasts"), ANNOUNCEMENTS("Announcements"), EVENTS("Events") }

/**
 * A church's real public profile -- what was missing before was any way
 * to see everything a church has actually published (articles, podcasts,
 * announcements) in one place, not just events and members.
 */
@Composable
fun ChurchDetailScreen(
    churchId: String,
    onBack: () -> Unit,
    onRequireSignIn: () -> Unit = {},
    vm: ChurchDetailViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showCollaborateDialog by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(ChurchTab.ABOUT) }
    LaunchedEffect(churchId) { vm.load(churchId) }
    val church = state.church
    val isGuest = state.myId == "u_demo"

    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (church == null) {
            Text("Church not found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
            return@Column
        }

        // Header -- name, status, join/collaborate action wrapped in GlassCard
        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(church.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    if (church.verificationStatus == VerificationStatus.VERIFIED) {
                        Spacer(Modifier.width(6.dp))
                        VerifiedBadge(size = 18.dp)
                    }
                }
                Text(church.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text("Pastor: ${church.pastorName} \u00b7 ${church.beliefSystem.displayName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.People, null, tint = Gold400, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${state.members.size} members", style = MaterialTheme.typography.labelMedium, color = Gold400)
                    Spacer(Modifier.weight(1f))
                    MembershipAction(state, isGuest, onRequireSignIn, onJoin = { vm.joinChurch() }, onCollaborate = { showCollaborateDialog = true })
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${state.followerCount} following on Timeline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    // Follow button with gold gradient + shadow
                    Box(
                        Modifier
                            .shadow(
                                if (state.isFollowing) 0.dp else 6.dp,
                                RoundedCornerShape(20.dp),
                                ambientColor = Gold400.copy(alpha = 0.25f),
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (state.isFollowing) MaterialTheme.colorScheme.surfaceVariant
                                else Brush.horizontalGradient(GoldGradient)
                            )
                            .clickable(
                                indication = androidx.compose.material3.ripple(),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { if (isGuest) onRequireSignIn() else vm.toggleFollow() },
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            if (state.isFollowing) "Following" else "Follow",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.isFollowing) MaterialTheme.colorScheme.onSurface else Color(0xFF1A0F00),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                if (church.verificationStatus == VerificationStatus.PENDING) {
                    Spacer(Modifier.height(6.dp))
                    StatusPill("Verification Pending", Gold400)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = tab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Gold400,
            edgePadding = 24.dp,
        ) {
            ChurchTab.values().forEach { t ->
                Tab(selected = tab == t, onClick = { tab = t }, text = { Text(t.label, color = if (tab == t) Gold400 else MaterialTheme.colorScheme.onSurfaceVariant) })
            }
        }

        when (tab) {
            ChurchTab.ABOUT -> AboutTab(church.description)
            ChurchTab.ARTICLES -> ListTab(state.articles, "No articles published yet") { ArticleRow(it) }
            ChurchTab.PODCASTS -> ListTab(state.podcasts, "No podcasts published yet") { PodcastRow(it) }
            ChurchTab.ANNOUNCEMENTS -> ListTab(state.announcements, "No announcements yet") { AnnouncementRow(it) }
            ChurchTab.EVENTS -> ListTab(state.events, "No upcoming events yet") { EventRow(it) }
        }
    }

    if (showCollaborateDialog) {
        CollaborateDialog(
            churchName = church?.name ?: "",
            onSend = { message -> vm.requestCollaboration(message) { showCollaborateDialog = false } },
            onDismiss = { showCollaborateDialog = false },
        )
    }
}

@Composable
private fun MembershipAction(
    state: ChurchDetailState, isGuest: Boolean, onRequireSignIn: () -> Unit,
    onJoin: () -> Unit, onCollaborate: () -> Unit,
) {
    val membership = state.myMembership
    if (state.myAccountType != AccountType.PERSONAL) {
        val sent = state.myCollaborationRequest
        when {
            sent == null -> GoldButton("Collaborate", onClick = { if (isGuest) onRequireSignIn() else onCollaborate() })
            sent.status == CollaborationStatus.PENDING -> StatusPill("Request Sent", Gold400)
            sent.status == CollaborationStatus.ACCEPTED -> StatusPill("Collaborating", MaterialTheme.colorScheme.tertiary)
            else -> GoldButton("Request Again", onClick = { if (isGuest) onRequireSignIn() else onCollaborate() })
        }
    } else when {
        membership == null -> GoldButton("Request Membership", onClick = { if (isGuest) onRequireSignIn() else onJoin() })
        membership.status == MemberStatus.PENDING -> StatusPill("Pending Approval", Gold400)
        membership.status == MemberStatus.APPROVED -> StatusPill("Member", MaterialTheme.colorScheme.tertiary)
        membership.status == MemberStatus.REJECTED -> GoldButton("Request Again", onClick = { if (isGuest) onRequireSignIn() else onJoin() })
        else -> {}
    }
}

@Composable
private fun StatusPill(text: String, accentColor: Color) {
    Box(
        Modifier
            .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = accentColor.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(20.dp))
            .background(Slate850.copy(alpha = 0.92f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = accentColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AboutTab(description: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                description.ifBlank { "This church hasn't added a description yet." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun <T> ListTab(items: List<T>, emptyText: String, row: @Composable (T) -> Unit) {
    if (items.isEmpty()) {
        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(20.dp))
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    row(item)
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(article: ArticleEntity) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(article.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Text(article.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        Spacer(Modifier.height(6.dp))
        Text("${article.likeCount} likes \u00b7 ${article.commentCount} comments", style = MaterialTheme.typography.labelSmall, color = Gold400)
    }
}

@Composable
private fun PodcastRow(series: PodcastSeriesEntity) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Podcasts, null, tint = Gold400, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(series.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(series.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AnnouncementRow(a: AnnouncementEntity) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Campaign, null, tint = Gold400, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(a.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(4.dp))
        Text(a.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
    }
}

@Composable
private fun EventRow(event: ChurchEventEntity) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.CalendarMonth, null, tint = Gold400, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(event.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(
                SimpleDateFormat("EEE, MMM d \u00b7 h:mm a", Locale.getDefault()).format(Date(event.startTime)) + if (event.isOnline) " \u00b7 Online" else "",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CollaborateDialog(churchName: String, onSend: (String) -> Unit, onDismiss: () -> Unit) {
    var message by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onDismiss)) {
        GlassCard(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(20.dp)
                    .clickable(enabled = false) {},
            ) {
                Column {
                    Text("Propose Collaboration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text("Suggest partnering with $churchName on an event, debate, or discussion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = message, onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("What would you like to collaborate on?") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Gold400,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    GoldButton("Send Request", onClick = { if (message.isNotBlank()) onSend(message) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
