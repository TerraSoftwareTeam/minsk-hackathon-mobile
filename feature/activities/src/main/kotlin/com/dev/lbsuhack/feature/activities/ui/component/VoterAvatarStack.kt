package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val MAX_STACK = 4

@Composable
internal fun VoterAvatarStack(voters: List<String>) {
    val shown = voters.take(MAX_STACK)
    val extra = voters.size - shown.size
    val size = 26.dp
    val overlap = 8.dp
    val count = shown.size + if (extra > 0) 1 else 0
    Box(modifier = Modifier.width(size + (size - overlap) * (count - 1))) {
        shown.forEachIndexed { index, voter ->
            VoterAvatar(name = voter, size = size, modifier = Modifier.offset(x = (size - overlap) * index))
        }
        if (extra > 0) {
            VoterBubble(text = "+$extra", size = size, modifier = Modifier.offset(x = (size - overlap) * shown.size))
        }
    }
}
