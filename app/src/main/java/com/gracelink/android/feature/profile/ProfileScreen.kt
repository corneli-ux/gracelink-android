package com.gracelink.android.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GlassCard
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.GoldGradient
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.ContentLanguage

@Composable
fun ProfileScreen(
    onNavigateToFaith: () -> Unit = {},
    onNavigateToArticles: () -> Unit = {},
    onNavigateToChurches: () -> Unit = {},
    onNavigateToPastors: () -> Unit = {},
    onNavigateToChurchPortal: () -> Unit = {},
    onNavigateToPastorPortal: () -> Unit = {},
    onOpenMyChurchProfile: (String) -> Unit = {},
    onSetupProfile: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val user = state.user

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) vm.uploadProfilePhoto(uri)
    }

    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(), contentPadding = PaddingValues(bottom = 24.dp)) {
        // ── Profile header ───────────────────────────────────────────────────────
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Box(
                        Modifier
                            .size(72.dp)
                            .shadow(12.dp, CircleShape, ambientColor = Gold400.copy(alpha = 0.2f))
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Gold400.copy(alpha = 0.2f), MaterialTheme.colorScheme.surfaceVariant)))
                            .clickable(enabled = user != null) { photoPicker.launch("image/*") },
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            state.isUploadingPhoto -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Gold400, strokeWidth = 2.dp)
                            user?.photoUrl != null -> coil.compose.AsyncImage(model = user.photoUrl, contentDescription = "Profile photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            else -> Text(user?.displayName?.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF1A1408), fontWeight = FontWeight.Bold)
                        }
                        if (user != null && !state.isUploadingPhoto) {
                            Box(Modifier.align(Alignment.BottomEnd).size(22.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.CameraAlt, "Change photo", tint = Gold400, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user?.displayName ?: "Not set up yet", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                        Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (user != null) {
                            Spacer(Modifier.height(4.dp))
                            val roleLabel = when (user.accountType) {
                                AccountType.CHURCH -> "CHURCH"
                                AccountType.PASTOR -> "PASTOR"
                                AccountType.PERSONAL -> "MEMBER"
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.horizontalGradient(GoldGradient))
                                        .let { m -> if (user.accountType == AccountType.CHURCH && state.myChurchId != null) m.clickable { onOpenMyChurchProfile(state.myChurchId!!) } else m }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(roleLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1A0F00))
                                }
                                if (user.isVerified) {
                                    Spacer(Modifier.width(6.dp))
                                    com.gracelink.android.core.components.VerifiedBadge(size = 14.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Photo upload error ──────────────────────────────────────────────────────
        if (state.photoUploadError != null) {
            item { Text(state.photoUploadError ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) }
        }

        // ── Profile setup banner ───────────────────────────────────────────────────
        if (user == null) {
            item {
                GoldButton("Set up your profile", onClick = onSetupProfile, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── Stats row ────────────────────────────────────────────────────────────────
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(Icons.Rounded.GraphicEq, "Minutes", "${user?.totalMinutes ?: 0}", Gold400, Modifier.weight(1f))
                StatCard(Icons.Rounded.TrendingUp, "Streak", "${user?.streakDays ?: 0}d", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                StatCard(Icons.Rounded.Bookmark, "Saved", "${state.favoritesCount}", Gold400, Modifier.weight(1f))
                StatCard(Icons.Rounded.Download, "Downloads", "${state.downloadsCount}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Prayers offered ──────────────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Spa, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Prayers Offered", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        Text("Standing with brothers and sisters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${user?.prayersOffered ?: 0}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── My Faith section ────────────────────────────────────────────────────────
        item {
            Text("My Faith", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    Clickable(Icons.Rounded.Spa, "Faith Journey", "Track your sanctification & belief system", onNavigateToFaith)
                    SettingsDivider()
                    Clickable(Icons.Rounded.Article, "My Articles", "Write and manage your articles", onNavigateToArticles)
                    SettingsDivider()
                    Clickable(Icons.Rounded.Church, "Find Churches", "Join a church & become a member", onNavigateToChurches)
                    SettingsDivider()
                    Clickable(Icons.Rounded.Person, "Find Pastors", "Follow individual pastors", onNavigateToPastors)
                    if (user?.accountType == AccountType.CHURCH) {
                        SettingsDivider()
                        Clickable(Icons.Rounded.Church, "Church Portal", "Manage members, events & articles", onNavigateToChurchPortal)
                    }
                    if (user?.accountType == AccountType.PASTOR) {
                        SettingsDivider()
                        Clickable(Icons.Rounded.Mic, "Pastor Studio", "Podcasts, live spaces, articles & radio slots", onNavigateToPastorPortal)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Settings ──────────────────────────────────────────────────────────────────
        item {
            Text("Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    var notif by remember(user?.uid) { mutableStateOf(user?.notificationsEnabled ?: true) }
                    var dataSaver by remember(user?.uid) { mutableStateOf(user?.dataSaverEnabled ?: false) }
                    Toggle(Icons.Rounded.Notifications, "Push Notifications", "Live event reminders + new content", notif) { notif = it; vm.setNotifications(it) }
                    SettingsDivider()
                    Toggle(Icons.Rounded.Savings, "Data Saver", "Lower thumbnail quality, no auto-play", dataSaver) { dataSaver = it; vm.setDataSaver(it) }
                    SettingsDivider()
                    LanguageRow(user?.preferredLanguage ?: ContentLanguage.EN) { vm.setLanguage(it) }
                    SettingsDivider()
                    Clickable(Icons.Rounded.Download, "Downloads Manager", "${state.downloadsCount} items \u2022 Manage storage", onOpenDownloads)
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Sign out ──────────────────────────────────────────────────────────────────
        item {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                    .clickable { vm.signOut(); onSignedOut() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Text("GraceLink v1.0.0\nBuilt with care for the body of Christ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String, tint: Color, modifier: Modifier) {
    GlassCard(modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun Clickable(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Gold400.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Gold400, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface); Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun Toggle(icon: ImageVector, title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Gold400.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Gold400, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface); Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedThumbColor = Gold400, checkedTrackColor = Gold400.copy(alpha = 0.4f), uncheckedTrackColor = MaterialTheme.colorScheme.background))
    }
}

@Composable private fun LanguageRow(current: ContentLanguage, onPick: (ContentLanguage) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Gold400.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Language, null, tint = Gold400, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text("Preferred Language", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface); Text("Content will prioritize this language", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        ContentLanguage.values().forEach { lang ->
            Box(Modifier.padding(start = 6.dp).clip(RoundedCornerShape(8.dp)).background(if (lang == current) Brush.horizontalGradient(GoldGradient) else Brush.horizontalGradient(listOf(Slate800, Slate800))).clickable { onPick(lang) }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(lang.name, style = MaterialTheme.typography.labelSmall, color = if (lang == current) Color(0xFF1A1408) else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun SettingsDivider() { Box(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp).background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))) }
