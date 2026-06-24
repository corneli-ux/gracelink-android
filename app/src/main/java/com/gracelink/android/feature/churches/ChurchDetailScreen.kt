package com.gracelink.android.feature.churches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.data.db.entity.ChurchEventEntity
import com.gracelink.android.data.db.entity.ArticleEntity
import com.gracelink.android.data.db.entity.VerificationStatus
import com.gracelink.android.data.db.entity.MemberStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChurchDetailScreen(
    churchId: String,
    onBack: () -> Unit,
    vm: ChurchDetailViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(churchId) { vm.load(churchId) }
    val church = state.church

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Slate800).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(12.dp))
            Text("Church", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Church header
            item {
                church?.let { c ->
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Brush.horizontalGradient(listOf(Gold400.copy(alpha = 0.15f), Slate800))).padding(20.dp)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Church, null, tint = Gold400, modifier = Modifier.size(28.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(c.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                        if (c.verificationStatus == VerificationStatus.VERIFIED) { Spacer(Modifier.width(6.dp)); Icon(Icons.Rounded.CheckCircle, "Verified", tint = Emerald500, modifier = Modifier.size(18.dp)) }
                                    }
                                    Text(c.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Pastor: ${c.pastorName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Text("Belief: ${c.beliefSystem.displayName}", style = MaterialTheme.typography.bodySmall, color = Gold400)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.People, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("${state.members.size} members", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.weight(1f))
                                val membership = state.myMembership
                                when {
                                    membership == null -> {
                                        GoldButton("Request Membership", onClick = { vm.joinChurch() })
                                    }
                                    membership.status == MemberStatus.PENDING -> {
                                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Gold400.copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                            Text("⏳ Pending Approval", style = MaterialTheme.typography.labelMedium, color = Gold400, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    membership.status == MemberStatus.APPROVED -> {
                                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Emerald500.copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                            Text("✓ Member", style = MaterialTheme.typography.labelMedium, color = Emerald500, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    membership.status == MemberStatus.REJECTED -> {
                                        GoldButton("Request Again", onClick = { vm.joinChurch() })
                                    }
                                }
                            }
                            if (c.verificationStatus == VerificationStatus.PENDING) {
                                Spacer(Modifier.height(8.dp))
                                Text("⏳ Verification pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Upcoming events
            item { Text("Events", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface) }
            items(state.events) { event -> EventCard(event) }

            // Church articles
            item { Text("Articles from this Church", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface) }
            items(state.articles) { article -> ArticleCard(article) }

            // Members
            item { Text("Members (${state.members.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface) }
            items(state.members) { member ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Slate800).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Text(member.displayName.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.labelLarge, color = Gold400, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column { Text(member.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface); Text("Joined ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(member.joinedAt))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: ChurchEventEntity) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CalendarMonth, null, tint = Gold400, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(event.startTime)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (event.isOnline && event.meetingLink != null) Text("🔗 Online meeting", style = MaterialTheme.typography.labelSmall, color = Emerald500)
            }
        }
    }
}

@Composable
private fun ArticleCard(article: ArticleEntity) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(14.dp)) {
        Text(article.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Text(article.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        Spacer(Modifier.height(8.dp))
        Row {
            Text("❤️ ${article.likeCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text("💬 ${article.commentCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
