package com.gracelink.android.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Emerald500

/**
 * Faith Link logo — gold cross + emerald sound waves.
 * Used on splash, onboarding, auth, and anywhere the brand mark appears.
 */
@Composable
fun FaithLinkLogo(
    modifier: Modifier = Modifier,
    size: Int = 80,
    gold: Color = Gold500,
    emerald: Color = Emerald500,
) {
    Canvas(modifier.size(size.dp)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w * 0.42f
        val cy = h * 0.5f

        // ── Cross (gold) ────────────────────────────────────────────────────
        val crossScale = 0.38f
        val crossW = w * crossScale
        val crossH = h * crossScale * 1.8f
        val barW = crossW * 0.9f
        val barH = crossH * 0.22f

        // Vertical bar
        drawRect(
            color = gold,
            topLeft = Offset(cx - barH / 2, cy - crossH / 2),
            size = androidx.compose.ui.geometry.Size(barH, crossH),
        )
        // Horizontal bar
        drawRect(
            color = gold,
            topLeft = Offset(cx - barW / 2, cy - crossH * 0.12f),
            size = androidx.compose.ui.geometry.Size(barW, barH),
        )

        // ── Sound waves (emerald) ───────────────────────────────────────────
        val waveStartX = cx + barW * 0.55f
        val waveCenterY = cy
        val strokeWidth = size * 0.018f

        // Three arcs of increasing size
        val arcs = listOf(
            Triple(waveStartX, 0.10f, 0.08f),
            Triple(waveStartX + w * 0.06f, 0.16f, 0.14f),
            Triple(waveStartX + w * 0.12f, 0.22f, 0.20f),
        )

        arcs.forEach { (startX, width, height) ->
            val arcW = w * width
            val arcH = h * height
            val arcCx = startX + arcW / 2
            val topY = waveCenterY - arcH / 2
            val bottomY = waveCenterY + arcH / 2
            val leftX = startX
            val rightX = startX + arcW

            val path = Path().apply {
                moveTo(leftX, topY)
                cubicTo(
                    leftX, waveCenterY - arcH * 0.15f,
                    rightX, waveCenterY - arcH * 0.15f,
                    rightX, topY
                )
                // bottom curve
                moveTo(leftX, bottomY)
                cubicTo(
                    leftX, waveCenterY + arcH * 0.15f,
                    rightX, waveCenterY + arcH * 0.15f,
                    rightX, bottomY
                )
            }
            drawPath(
                path = path,
                color = emerald,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}
