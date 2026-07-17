package com.gracelink.android.feature.portal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate850
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary
import com.gracelink.android.core.theme.TextMuted

/**
 * Unique post-login entry point.
 * Two clear portals + quick launch into core experiences.
 * Designed to feel completely different from typical Christian radio apps.
 */
@Composable
fun PortalHubScreen(
    onEnterMembers: () -> Unit,
    onEnterChurchPortal: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Obsidian, Color(0xFF0A0A0A), Slate900)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            // Header
            Text(
                text = "GraceLink",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = Gold400
            )
            Text(
                text = "Where faith finds its voice",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Primary Portals
            Text(
                text = "CHOOSE YOUR SPACE",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Members Portal Card
            PortalCard(
                title = "Members Portal",
                subtitle = "24/7 Radio • Podcasts • Live Spaces • Prayer",
                icon = Icons.Rounded.Groups,
                accent = Gold500,
                gradient = listOf(Slate850, Color(0xFF1A1610)),
                onClick = onEnterMembers
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Church Portal Card
            PortalCard(
                title = "Church Portal",
                subtitle = "Lead • Manage members • Host spaces • Schedule",
                icon = Icons.Rounded.Church,
                accent = Emerald500,
                gradient = listOf(Slate850, Color(0xFF0F1A14)),
                onClick = onEnterChurchPortal
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Quick Access
            Text(
                text = "QUICK LAUNCH",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickTile(
                    icon = Icons.Rounded.Radio,
                    label = "Faith FM",
                    modifier = Modifier.weight(1f),
                    onClick = onEnterMembers // lands on Radio via bottom nav
                )
                QuickTile(
                    icon = Icons.Rounded.Podcasts,
                    label = "Podcasts",
                    modifier = Modifier.weight(1f),
                    onClick = onEnterMembers
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickTile(
                    icon = Icons.Rounded.Headphones,
                    label = "Live Spaces",
                    modifier = Modifier.weight(1f),
                    onClick = onEnterMembers
                )
                QuickTile(
                    icon = Icons.Rounded.Spa,
                    label = "Prayer",
                    modifier = Modifier.weight(1f),
                    onClick = onEnterMembers
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Profile shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800.copy(alpha = 0.6f))
                    .clickable(onClick = onOpenProfile)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Gold500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You",
                        color = Gold400,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Your Profile", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                    Text("Membership • Settings • Sign out", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun PortalCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradient))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.4f), Color.Transparent)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = accent.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun QuickTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Slate850)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = Gold400, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}
