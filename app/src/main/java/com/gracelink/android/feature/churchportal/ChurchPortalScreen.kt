package com.gracelink.android.feature.churchportal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary

/**
 * Church leadership / admin portal.
 * Appears after login for church owners or when user opens it from membership.
 * Clean dashboard for managing the church presence on GraceLink.
 */
@Composable
fun ChurchPortalScreen(
    onBack: () -> Unit,
    onManageMembers: () -> Unit,
    onScheduleRadio: () -> Unit,
    onStartSpace: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Church Portal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Status card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1A2A1A), Slate850)
                        )
                    )
                    .border(1.dp, Emerald500.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Verified, null, tint = Emerald500, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Your Church", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text("Verification: Pending review", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text("Members: 0 • Spaces hosted: 0", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "MANAGE",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            PortalAction(
                title = "Members",
                subtitle = "View & manage church members",
                icon = Icons.Rounded.Groups,
                accent = Gold500,
                onClick = onManageMembers
            )

            Spacer(modifier = Modifier.height(10.dp))

            PortalAction(
                title = "Radio Schedule",
                subtitle = "Claim slots on GraceLink Radio 24/7",
                icon = Icons.Rounded.Radio,
                accent = Gold400,
                onClick = onScheduleRadio
            )

            Spacer(modifier = Modifier.height(10.dp))

            PortalAction(
                title = "Start Live Space",
                subtitle = "Host a live audio room for your congregation",
                icon = Icons.Rounded.Headphones,
                accent = Emerald500,
                onClick = onStartSpace
            )

            Spacer(modifier = Modifier.height(10.dp))

            PortalAction(
                title = "Church Settings",
                subtitle = "Profile, belief, location, media",
                icon = Icons.Rounded.Settings,
                accent = Color(0xFF5AB8E0),
                onClick = { /* future */ }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PortalAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate850)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
