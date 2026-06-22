package com.gracelink.android.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape tokens. Spec calls for "rounded corners (12-16dp)".
 */
val GraceShapes = Shapes(
    extraSmall  = RoundedCornerShape(6.dp),
    small       = RoundedCornerShape(10.dp),
    medium      = RoundedCornerShape(14.dp),  // default card radius
    large       = RoundedCornerShape(20.dp),  // hero cards / bottom sheets
    extraLarge  = RoundedCornerShape(28.dp),  // FABs, top of sheets
)
