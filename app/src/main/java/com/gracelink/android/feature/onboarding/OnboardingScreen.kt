package com.gracelink.android.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.components.GhostButton

private data class Page(val icon: ImageVector, val title: String, val body: String)

private val pages = listOf(
    Page(Icons.Rounded.Headphones, "Listen", "Worship, teaching, and regional radio — streamed live and on-demand. Telugu and English, side by side."),
    Page(Icons.Rounded.RecordVoiceOver, "Participate", "Join live debates. Ask questions via text or voice. Share your testimony with a community that listens."),
    Page(Icons.Rounded.Spa, "Belong", "Post prayers, encourage others, follow pastors. Build genuine fellowship, one episode at a time."),
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pager = rememberPagerState(pageCount = { pages.size })

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.gracelink.android.R.drawable.faith_link_logo),
                    contentDescription = "Faith Link",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Faith Link", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(Modifier.height(40.dp))

            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { i ->
                val page = pages[i]
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(page.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    }
                    Spacer(Modifier.height(32.dp))
                    Text(
                        page.title,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        page.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Row(Modifier.padding(vertical = 20.dp), horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { i ->
                    val color = if (i == pager.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == pager.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LangChip("English", "EN", Modifier.weight(1f), true) {}
                LangChip("తెలుగు", "TE", Modifier.weight(1f), false) {}
            }

            Spacer(Modifier.height(14.dp))
            GoldButton("Get Started", onClick = onDone, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            GhostButton("Sign in with Google", onClick = onDone, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun LangChip(label: String, code: String, modifier: Modifier, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)) else Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(code, style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
        }
    }
}
