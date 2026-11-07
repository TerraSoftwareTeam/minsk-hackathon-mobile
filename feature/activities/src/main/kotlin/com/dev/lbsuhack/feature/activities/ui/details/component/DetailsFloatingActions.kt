package com.dev.lbsuhack.feature.activities.ui.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.presentation.ActivityRatingMode
import com.dev.lbsuhack.feature.activities.presentation.actionsFor
import com.dev.lbsuhack.feature.activities.ui.common.FloatingActionsBackdrop
import com.dev.lbsuhack.feature.activities.ui.component.ActivityRating
import com.dev.lbsuhack.feature.activities.ui.component.ActivityRatingSummary
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVoteButton

@Composable
internal fun DetailsFloatingActions(
    proposal: ActivityProposal,
    viewerRole: UserRole?,
    canInteract: Boolean,
    canVote: Boolean,
    onVote: () -> Unit,
    onUnvote: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onUnlike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = proposal.actionsFor(viewerRole)
    if (!actions.isVisible) return
    Box(modifier = modifier.fillMaxWidth().height(if (actions.showParticipation) 144.dp else 96.dp)) {
        FloatingActionsBackdrop(modifier = Modifier.matchParentSize())
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = if (actions.showParticipation) Arrangement.spacedBy(12.dp) else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (actions.ratingMode) {
                ActivityRatingMode.INTERACTIVE -> ActivityRating(
                    likesCount = proposal.likesCount,
                    likedByMe = proposal.likedByMe,
                    dislikedByMe = proposal.dislikedByMe,
                    enabled = canInteract,
                    onLike = onLike,
                    onDislike = onDislike,
                    onUnlike = onUnlike,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                )
                ActivityRatingMode.SUMMARY -> ActivityRatingSummary(score = proposal.likesCount)
                ActivityRatingMode.HIDDEN -> Unit
            }
            if (actions.showParticipation) {
                ActivityVoteButton(
                    proposal = proposal,
                    canInteract = canVote,
                    onVote = onVote,
                    onUnvote = onUnvote,
                    modifier = Modifier.weight(1f).height(60.dp),
                )
            }
        }
    }
}
