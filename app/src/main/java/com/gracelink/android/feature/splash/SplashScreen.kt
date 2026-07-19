package com.gracelink.android.feature.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gracelink.android.R
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Obsidian
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onComplete()
    }

    val transition = rememberInfiniteTransition(label = "splash")
    val scale by transition.animateFloat(
        initialValue = 0.97f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.15f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Obsidian),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // GraceLink logo
            Box(
                Modifier
                    .size(120.dp)
                    .scale(scale)
            ) {
                // Subtle glow behind logo
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(Gold400.copy(alpha = glowAlpha), Gold400.copy(alpha = 0f))
                            )
                        )
                )
                // The actual logo image
                Image(
                    painter = painterResource(id = R.drawable.faith_link_logo),
                    contentDescription = "GraceLink Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(20.dp))
            AnimatedVisibility(visible = true, enter = fadeIn(tween(800))) {
                Text(
                    "GraceLink",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.height(4.dp))
            AnimatedVisibility(visible = true, enter = fadeIn(tween(1200))) {
                Text(
                    "Listen • Participate • Belong",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.7f),
                )
            }
        }
    }
}
