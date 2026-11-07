package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.ui.activities.mapper.icon
import com.dev.lbsuhack.feature.activities.ui.activities.mapper.labelRes
import com.dev.lbsuhack.feature.activities.ui.component.ActivityTag

@Composable
internal fun ActivityMetadata(
    proposal: ActivityProposal,
    modifier: Modifier = Modifier,
    isAuthor: Boolean = false,
) {
    val foreground = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    val shape = MaterialTheme.shapes.medium
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                ActivityTag(
                    modifier = Modifier.fillMaxSize(),
                    text = stringResource(proposal.category.labelRes()),
                    icon = proposal.category.icon(),
                )
            }
            Box(modifier = Modifier.weight(1.3f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
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
                }
            }
        }
        if (isAuthor) {
            ActivityTag(text = stringResource(R.string.activity_created_by_me))
        }
    }
}
