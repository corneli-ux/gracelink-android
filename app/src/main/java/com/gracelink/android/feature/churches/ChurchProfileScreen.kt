package com.gracelink.android.feature.churches

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Verified
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
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.VerificationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChurchProfileScreen(
    onBack: () -> Unit,
    vm: ChurchProfileViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val church = state.church

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Slate800).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(12.dp))
            Text("Church Dashboard", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Church info card
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
                                    Text(c.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("${c.location} • Pastor ${c.pastorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // Verification status
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (c.verificationStatus) {
                                    VerificationStatus.VERIFIED -> {
                                        Icon(Icons.Rounded.Verified, null, tint = Emerald500, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Verified Church", style = MaterialTheme.typography.labelMedium, color = Emerald500, fontWeight = FontWeight.SemiBold)
                                    }
                                    VerificationStatus.PENDING -> {
                                        Text("⏳ Verification pending — upload certificate below", style = MaterialTheme.typography.labelMedium, color = Gold400)
                                        Spacer(Modifier.weight(1f))
                                        GoldButton("Verify", onClick = { vm.showVerification(true) })
                                    }
                                    else -> {
                                        Text("Not verified", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.weight(1f))
                                        GoldButton("Verify", onClick = { vm.showVerification(true) })
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // Stats row
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatCard("Members", "${state.members.size}", Icons.Rounded.People)
                                StatCard("Events", "${state.events.size}", Icons.Rounded.CalendarMonth)
                                StatCard("Articles", "${state.articles.size}", Icons.Rounded.Edit)
                            }
                        }
                    }
                }
            }

            // Quick actions
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionCard("Create Event", Icons.Rounded.CalendarMonth, Modifier.weight(1f)) { vm.showCreateEvent(true) }
                    ActionCard("Write Article", Icons.Rounded.Edit, Modifier.weight(1f)) { vm.showWriteArticle(true) }
                }
            }

            // Upcoming events
            item { Text("Events", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface) }
            items(state.events) { event ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Slate800).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarMonth, null, tint = Gold400, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(event.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(event.startTime)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (event.isOnline) Text("Online", style = MaterialTheme.typography.labelSmall, color = Emerald500)
                }
            }

            // Pending membership requests
            if (state.pendingMembers.isNotEmpty()) {
                item { Text("Membership Requests (${state.pendingMembers.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Gold400) }
                items(state.pendingMembers) { member ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Slate800).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text(member.displayName.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.labelLarge, color = Gold400, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(member.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("Requested ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(member.joinedAt))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Belief: ${member.beliefSystem.displayName}", style = MaterialTheme.typography.labelSmall, color = Gold400)
                        }
                        // Approve button
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Emerald500).clickable { vm.approveMember(member.id) }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text("✓", color = Color(0xFF002218), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(6.dp))
                        // Reject button
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.error).clickable { vm.rejectMember(member.id) }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text("✗", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Approved members
            item { Text("Members (${state.members.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface) }
            items(state.members) { member ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Slate800).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Text(member.displayName.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.labelLarge, color = Gold400, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(member.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Joined ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(member.joinedAt))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(member.beliefSystem.displayName, style = MaterialTheme.typography.labelSmall, color = Gold400)
                }
            }

            // Articles
            item { Text("Church Articles", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface) }
            items(state.articles) { article ->
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Slate800).padding(14.dp)) {
                    Text(article.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(article.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    Spacer(Modifier.height(6.dp))
                    Text("❤️ ${article.likeCount}  💬 ${article.commentCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Dialogs
    AnimatedVisibility(visible = state.showCreateEvent) {
        CreateEventDialog(onCreate = { t, d, ts, online, link, loc -> vm.createEvent(t, d, ts, online, link, loc) }, onDismiss = { vm.showCreateEvent(false) })
    }
    AnimatedVisibility(visible = state.showWriteArticle) {
        WriteArticleDialog(onPublish = { t, c -> vm.writeArticle(t, c) }, onDismiss = { vm.showWriteArticle(false) })
    }
    AnimatedVisibility(visible = state.showVerification) {
        VerificationDialog(onSubmit = { cert, photo -> vm.submitVerification(cert, photo) }, onDismiss = { vm.showVerification(false) })
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Gold400, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActionCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(14.dp)).background(Slate800).clickable(onClick = onClick).padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Gold400, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CreateEventDialog(onCreate: (String, String, Long, Boolean, String?, String?) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(true) }
    var link by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onDismiss)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(24.dp).clickable(enabled = false) {}) {
            Column {
                Text("Create Event", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Event title") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, modifier = Modifier.fillMaxWidth().height(100.dp), placeholder = { Text("Description") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { Text("Online meeting", modifier = Modifier.weight(1f)); Switch(checked = isOnline, onCheckedChange = { isOnline = it }, colors = SwitchDefaults.colors(checkedThumbColor = Gold400)) }
                if (isOnline) { Spacer(Modifier.height(10.dp)); OutlinedTextField(value = link, onValueChange = { link = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Meeting link (Zoom/Meet)") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp)) }
                else { Spacer(Modifier.height(10.dp)); OutlinedTextField(value = loc, onValueChange = { loc = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Location") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp)) }
                Spacer(Modifier.height(16.dp))
                GoldButton("Create Event", onClick = { if (title.isNotBlank()) onCreate(title, desc, System.currentTimeMillis() + 86400000, isOnline, if (isOnline) link.ifBlank { null } else null, if (!isOnline) loc.ifBlank { null } else null) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun WriteArticleDialog(onPublish: (String, String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onDismiss)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(24.dp).clickable(enabled = false) {}) {
            Column {
                Text("Write to Members", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Title") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, modifier = Modifier.fillMaxWidth().height(150.dp), placeholder = { Text("Write your message to church members...") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(16.dp))
                GoldButton("Publish to Members", onClick = { if (title.isNotBlank() && content.isNotBlank()) onPublish(title, content) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun VerificationDialog(onSubmit: (String, String) -> Unit, onDismiss: () -> Unit) {
    var certUrl by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onDismiss)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(24.dp).clickable(enabled = false) {}) {
            Column {
                Text("Church Verification", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("Provide your church registration certificate and a photo of your church building for verification.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = certUrl, onValueChange = { certUrl = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Certificate URL (PDF/Image link)") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Church photo URL") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(16.dp))
                GoldButton("Submit for Verification", onClick = { onSubmit(certUrl, photoUrl) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
