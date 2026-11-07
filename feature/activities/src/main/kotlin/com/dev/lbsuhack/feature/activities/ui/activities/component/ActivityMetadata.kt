package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.ui.activities.mapper.icon
import com.dev.lbsuhack.feature.activities.ui.activities.mapper.labelRes
import com.dev.lbsuhack.feature.activities.ui.component.ActivityStatusChip
import com.dev.lbsuhack.feature.activities.ui.component.ActivityTag

@Composable
internal fun ActivityMetadata(
    proposal: ActivityProposal,
    modifier: Modifier = Modifier,
) {
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
            ActivityStatusChip(
                status = proposal.status,
                modifier = Modifier.weight(1.3f).fillMaxHeight(),
            )
        }
    }
}
