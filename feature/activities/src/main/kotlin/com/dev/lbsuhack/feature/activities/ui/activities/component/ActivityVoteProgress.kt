package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVotersSummary
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVotesMeter

@Composable
internal fun ActivityVoteProgress(proposal: ActivityProposal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.large,
            )
            .padding(14.dp),
    ) {
        ActivityVotesMeter(proposal = proposal)

        Spacer(modifier = Modifier.height(12.dp))

        ActivityVotersSummary(voters = proposal.voters)
    }
}
