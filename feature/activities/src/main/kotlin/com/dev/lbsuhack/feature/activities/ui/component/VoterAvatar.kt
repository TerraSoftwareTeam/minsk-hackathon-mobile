package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun VoterAvatar(name: String, size: Dp, modifier: Modifier = Modifier) {
    VoterBubble(text = initials(name), size = size, modifier = modifier)
}
