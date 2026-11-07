package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dev.lbsuhack.core.ui.component.HackBadge

@Composable
internal fun ActivityAuthorBadge(text: String, modifier: Modifier = Modifier) {
    HackBadge(text = text, modifier = modifier)
}
