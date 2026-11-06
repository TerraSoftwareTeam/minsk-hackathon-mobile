package com.dev.lbsuhack.feature.activities.ui.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.ui.component.ActivityTag

@Composable
internal fun ActivityMetadata(proposal: ActivityProposal, modifier: Modifier = Modifier, onPhoto: Boolean = false) {
    val foreground = if (onPhoto) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val secondary = if (onPhoto) foreground.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            ActivityTag(
                modifier = Modifier.fillMaxSize(),
                text = stringResource(proposal.category.labelRes()),
                icon = proposal.category.icon(),
                onPhoto = onPhoto,
            )
        }
        Box(modifier = Modifier.weight(1.3f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (onPhoto) MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f)
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape,
                    )
                    .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f), shape)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = stringResource(proposal.status.labelRes()),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = foreground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(proposal.initiative.labelRes()),
                    style = MaterialTheme.typography.labelSmall,
                    color = secondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
