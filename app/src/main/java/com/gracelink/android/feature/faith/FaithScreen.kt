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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.data.db.entity.BeliefSystem

/** Minimalist: flat background, hairline dividers instead of card boxes,
 * theme colors instead of hardcoded Gold/Slate/Emerald hex values. */
@Composable
fun FaithScreen(onRequireSignIn: () -> Unit = {}, vm: FaithViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val progress = state.progress

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollStateCompat()).background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Text("Faith Journey", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp))
        Text("Track your sanctification progress", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 20.dp, bottom = 16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Belief system
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text("YOUR BELIEF SYSTEM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(state.beliefSystem.displayName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(getBeliefDescription(state.beliefSystem), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (progress != null) {
            // Sanctification progress
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sanctification Level", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text("${progress.sanctificationLevel}%", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.sanctificationLevel / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                val daysLeft = ((progress.gracePeriodEndsAt - System.currentTimeMillis()) / (24 * 3600 * 1000)).coerceAtLeast(0)
                Text("Grace period: $daysLeft days remaining to progress", style = MaterialTheme.typography.bodySmall, color = if (daysLeft < 7) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Stats
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                StatRow("Bible Reading", "${progress.bibleReadingDays} days", Icons.Rounded.AutoStories)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                StatRow("Prayer sessions", "${progress.prayerSessions}\u00d7", Icons.Rounded.Spa)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                StatRow("Church attendance", "${progress.churchAttendances}\u00d7", Icons.Rounded.AutoStories)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                StatRow("Members discipled", "${progress.membersDiscipled}", Icons.Rounded.Spa)
            }
            Spacer(Modifier.height(20.dp))

            // Quick log buttons
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { vm.logBibleReading() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1A1408))) { Text("Log Bible Reading") }
                Button(onClick = { vm.logPrayer() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color(0xFF002218))) { Text("Log Prayer") }
            }
            Spacer(Modifier.height(20.dp))
        } else {
            // Guest state: belief descriptions are useful without an account,
            // but progress tracking needs one -- make that clear instead of
            // silently showing nothing.
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRequireSignIn)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Login, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Sign in to track your journey", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text("Log Bible reading, prayer, and sanctification progress", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, icon: ImageVector) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
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
