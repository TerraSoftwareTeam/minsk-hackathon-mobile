package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.dev.lbsuhack.core.ui.component.HackTag

@Composable
internal fun ActivityTag(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    HackTag(text = text, modifier = modifier, icon = icon)
}
