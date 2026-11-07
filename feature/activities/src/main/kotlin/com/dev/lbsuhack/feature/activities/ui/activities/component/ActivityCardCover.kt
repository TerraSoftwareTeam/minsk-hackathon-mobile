package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.ui.component.ActivityPhoto

@Composable
internal fun ActivityCardCover(proposal: ActivityProposal, photoUri: String) {
    val scrim = MaterialTheme.colorScheme.scrim
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.6f)) {
        ActivityPhoto(
            photoUri = photoUri,
            title = proposal.title,
            modifier = Modifier.matchParentSize(),
            shape = RectangleShape,
        )
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.verticalGradient(0f to scrim.copy(alpha = 0.18f), 0.45f to scrim.copy(alpha = 0f)),
            ),
        )
        ActivityMetadata(
            proposal = proposal,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        )
    }
}
