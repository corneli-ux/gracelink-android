package com.gracelink.android.feature.faith

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GlassCard
import com.gracelink.android.core.theme.Emerald400
import com.gracelink.android.core.theme.EmeraldGradient
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.GoldGradient
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary
import com.gracelink.android.data.db.entity.BeliefSystem

/** GraceLink Faith screen — glass card design with gold accents,
 * gradient progress bar, and branded header. */
@Composable
fun FaithScreen(onRequireSignIn: () -> Unit = {}, vm: FaithViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val progress = state.progress

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollStateCompat()).background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        // ── Header ──────────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "GRACELINK",
                style = MaterialTheme.typography.labelMedium,
                color = Gold400,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            "Faith Journey",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Gold400,
            modifier = Modifier.padding(start = 24.dp, bottom = 4.dp),
        )
        Text(
            "Track your sanctification progress",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 24.dp, bottom = 16.dp),
        )

        // ── Belief system — glass card ──────────────────────────────────────────
        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "YOUR BELIEF SYSTEM",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Gold400,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    state.beliefSystem.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    getBeliefDescription(state.beliefSystem),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (progress != null) {
            // ── Sanctification progress — glass card with gold gradient bar ──────
            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Sanctification Level",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${progress.sanctificationLevel}%",
                            style = MaterialTheme.typography.titleLarge,
                            color = Gold400,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // Gold gradient progress bar
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress.sanctificationLevel / 100f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(GoldGradient)),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    val daysLeft = ((progress.gracePeriodEndsAt - System.currentTimeMillis()) / (24 * 3600 * 1000)).coerceAtLeast(0)
                    Text(
                        "Grace period: $daysLeft days remaining to progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (daysLeft < 7) MaterialTheme.colorScheme.error else TextMuted,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Stats — glass cards with gold icon containers ────────────────────
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatGlassCard(
                    icon = Icons.Rounded.AutoStories,
                    label = "Bible Reading",
                    value = "${progress.bibleReadingDays} days",
                    modifier = Modifier.weight(1f),
                )
                StatGlassCard(
                    icon = Icons.Rounded.Spa,
                    label = "Prayer Sessions",
                    value = "${progress.prayerSessions}\u00d7",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatGlassCard(
                    icon = Icons.Rounded.AutoStories,
                    label = "Church Attendance",
                    value = "${progress.churchAttendances}\u00d7",
                    modifier = Modifier.weight(1f),
                )
                StatGlassCard(
                    icon = Icons.Rounded.Spa,
                    label = "Members Discipled",
                    value = "${progress.membersDiscipled}",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(20.dp))

            // ── Quick log buttons — shadow + gold gradient backgrounds ───────────
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GoldGradientButton(
                    text = "Log Bible Reading",
                    onClick = { vm.logBibleReading() },
                    modifier = Modifier.weight(1f),
                )
                EmeraldGradientButton(
                    text = "Log Prayer",
                    onClick = { vm.logPrayer() },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(20.dp))
        } else {
            // ── Guest state — sign-in prompt in glass card ───────────────────────
            // Belief descriptions are useful without an account, but progress
            // tracking needs one — make that clear.
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .clickable(
                        indication = ripple(),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onRequireSignIn,
                    ),
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(GoldGradient)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Login, null, tint = Color(0xFF1A0F00), modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Sign in to track your journey",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                        )
                        Text(
                            "Log Bible reading, prayer, and sanctification progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

// ── Stat glass card — metric display with gold icon container ──────────────────
@Composable
private fun StatGlassCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(GoldGradient)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Color(0xFF1A0F00), modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

// ── Gold gradient quick-action button ──────────────────────────────────────────
@Composable
private fun GoldGradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Gold400.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(GoldGradient))
            .clickable(
                indication = ripple(color = Color(0xFF1A0F00)),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Color(0xFF1A0F00),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

// ── Emerald gradient quick-action button ───────────────────────────────────────
@Composable
private fun EmeraldGradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Emerald400.copy(alpha = 0.20f))
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(EmeraldGradient))
            .clickable(
                indication = ripple(color = Color(0xFF002218)),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Color(0xFF002218),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
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
