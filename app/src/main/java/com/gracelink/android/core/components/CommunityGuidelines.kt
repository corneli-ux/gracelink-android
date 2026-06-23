package com.gracelink.android.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gracelink.android.core.theme.Emerald500
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Slate900

@Composable
fun CommunityGuidelinesSheet(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onDismiss)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(24.dp).clickable(enabled = false) {}) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Spa, null, tint = Gold500, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Community Guidelines", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onDismiss() }.size(24.dp))
                }
                Spacer(Modifier.height(16.dp))
                GuidelineItem("Be respectful", "Treat others with love and kindness, as Christ loved us.")
                GuidelineItem("No profanity", "Keep language clean and edifying.")
                GuidelineItem("No spam", "Don't post repetitive or promotional content.")
                GuidelineItem("Stay on topic", "Prayers and questions should be faith-related.")
                GuidelineItem("Report violations", "Use the flag button on any inappropriate content.")
                Spacer(Modifier.height(20.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Gold500).clickable { onDismiss() }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("I Understand", color = Color(0xFF1A0F00), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun GuidelineItem(title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Emerald500))
        Spacer(Modifier.width(12.dp))
        Column { Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface); Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun ReportButton(onClick: () -> Unit) {
    Row(Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Flag, "Report", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text("Report", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
