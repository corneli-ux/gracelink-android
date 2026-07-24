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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.components.GhostButton
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate850
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary

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
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, Slate850, MaterialTheme.colorScheme.surfaceVariant)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.gracelink.android.R.drawable.faith_link_logo),
                    contentDescription = "GraceLink",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("GraceLink", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
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
                            .size(120.dp)
                            .shadow(16.dp, CircleShape, ambientColor = Gold400.copy(alpha = 0.2f))
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(page.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
                    }
                    Spacer(Modifier.height(32.dp))
                    Text(
                        page.title,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        page.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Page indicators — gold dot for current
            Row(Modifier.padding(vertical = 20.dp), horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { i ->
                    val isSelected = i == pager.currentPage
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .shadow(if (isSelected) 4.dp else 0.dp, CircleShape)
                            .background(if (isSelected) Gold400 else Slate800)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            GoldButton("Get Started", onClick = onDone, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            GhostButton("Sign in with Google", onClick = onDone, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
        }
    }
}
