package com.dev.lbsuhack.feature.activities.ui.details.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVotesMeter

@Composable
internal fun VotingStatusCard(proposal: ActivityProposal, modifier: Modifier = Modifier) {
    DetailsCard(modifier = modifier) {
        ActivityVotesMeter(proposal = proposal, prominent = true, showDeadline = false)
    }
}
