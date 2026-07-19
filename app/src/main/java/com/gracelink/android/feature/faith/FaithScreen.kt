package com.gracelink.android.feature.faith

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.BeliefSystem

@Composable
fun FaithScreen(onRequireSignIn: () -> Unit = {}, vm: FaithViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val progress = state.progress

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollStateCompat()).background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Text("Faith Journey", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp))
        Text("Track your sanctification progress", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 20.dp, bottom = 16.dp))

        // Belief system card
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp)).background(Brush.horizontalGradient(listOf(Gold400.copy(alpha = 0.15f), Slate800))).padding(16.dp)) {
            Column {
                Text("Your Belief System", style = MaterialTheme.typography.labelMedium, color = Gold400, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(state.beliefSystem.displayName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(getBeliefDescription(state.beliefSystem), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(16.dp))

        if (progress != null) {
            // Sanctification progress bar
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp)).background(Slate800).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sanctification Level", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text("${progress.sanctificationLevel}%", style = MaterialTheme.typography.titleLarge, color = Gold400, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { progress.sanctificationLevel / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = Gold400, trackColor = Slate900)
                    Spacer(Modifier.height(8.dp))
                    val daysLeft = ((progress.gracePeriodEndsAt - System.currentTimeMillis()) / (24 * 3600 * 1000)).coerceAtLeast(0)
                    Text("Grace period: $daysLeft days remaining to progress", style = MaterialTheme.typography.bodySmall, color = if (daysLeft < 7) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Stats grid
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Bible Reading", "${progress.bibleReadingDays} days", Icons.Rounded.AutoStories, Gold400, Modifier.weight(1f))
                StatCard("Prayer", "${progress.prayerSessions}x", Icons.Rounded.Spa, Emerald500, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Church", "${progress.churchAttendances}x", Icons.Rounded.AutoStories, Gold400, Modifier.weight(1f))
                StatCard("Discipled", "${progress.membersDiscipled}", Icons.Rounded.Spa, Emerald500, Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))

            // Quick log buttons
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { vm.logBibleReading() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Gold400, contentColor = Color(0xFF1A1408))) { Text("Log Bible Reading") }
                Button(onClick = { vm.logPrayer() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color(0xFF002218))) { Text("Log Prayer") }
            }
            Spacer(Modifier.height(20.dp))
        } else {
            // Guest state: belief descriptions are useful without an account,
            // but progress tracking needs one -- make that clear instead of
            // silently showing nothing.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Gold400)
                    .clickable(onClick = onRequireSignIn)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Login, null, tint = Color(0xFF1A0F00))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Sign in to track your journey", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1A0F00))
                    Text("Log Bible reading, prayer, and sanctification progress", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1A0F00).copy(alpha = 0.75f))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(Slate800).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun rememberScrollStateCompat() = androidx.compose.foundation.rememberScrollState()

private fun getBeliefDescription(b: BeliefSystem): String = when (b) {
    BeliefSystem.PROGRESSIVE_SANCTIFICATION -> "The lifelong process of becoming more holy through the Holy Spirit's work, cooperation with God, and spiritual disciplines."
    BeliefSystem.REFORMED -> "Sovereign grace, predestination, and the authority of Scripture. Emphasis on God's initiative in salvation."
    BeliefSystem.ARMINIAN -> "Conditional election, resistible grace, and human free will cooperating with God's offer of salvation."
    BeliefSystem.WESLEYAN -> "Entire sanctification and holiness of heart and life. Pursuing Christian perfection through love."
    BeliefSystem.DISPENSATIONAL -> "Literal interpretation of Scripture, distinction between Israel and the Church, and prophetic timeline."
    BeliefSystem.COVENANT -> "Continuity between Old and New Testaments through God's covenant of grace with His people."
    BeliefSystem.PENTECOSTAL -> "Baptism in the Holy Spirit, spiritual gifts, and the ongoing work of the Spirit in believers' lives."
    BeliefSystem.BAPTIST -> "Believer's baptism, congregational governance, and the priesthood of all believers."
    BeliefSystem.LUTHERAN -> "Justification by faith alone, sacramental grace, and the authority of Word and Sacrament."
    BeliefSystem.ANGLICAN -> "Via media \u2014 Scripture, tradition, and reason in balance. Liturgical worship and episcopal governance."
    BeliefSystem.ORTHODOX -> "Theosis \u2014 becoming like God through participation in divine energies. Liturgical and mystical tradition."
    BeliefSystem.NONDENOMINATIONAL -> "Bible-centered faith focused on the essentials of the gospel, unity in Christ, and Spirit-led living."
}
