package com.gracelink.android.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gracelink.android.core.theme.PlatinumGradient

/**
 * Platinum verification badge -- deliberately not the app's regular gold
 * accent, so a verified church stands out distinctly rather than blending
 * into every other gold-tinted element on screen. For now this shows
 * whenever a church's verificationStatus is VERIFIED; later this is meant
 * to gate on a subscription rather than verification alone.
 *
 * A Compose Icon's tint only accepts a flat Color, not a Brush, so a
 * gradient-filled checkmark glyph isn't directly achievable that way --
 * this uses the standard verified-badge pattern instead: a small gradient-
 * filled circle with a plain white check inside, same as most platforms'
 * verification badges actually work.
 */
@Composable
fun VerifiedBadge(size: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(PlatinumGradient)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Check,
            contentDescription = "Verified",
            tint = Color.White,
            modifier = Modifier.size(size * 0.65f),
        )
    }
}
