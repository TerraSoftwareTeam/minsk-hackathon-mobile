package com.dev.lbsuhack.feature.activities.ui.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.ui.common.FloatingActionsBackdrop
import com.dev.lbsuhack.feature.activities.ui.component.ActivityRating
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVoteButton

@Composable
internal fun DetailsFloatingActions(
    proposal: ActivityProposal,
    isAuthor: Boolean,
    canInteract: Boolean,
    canVote: Boolean,
    onVote: () -> Unit,
    onUnvote: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onUnlike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(180.dp)) {
        FloatingActionsBackdrop(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (isAuthor) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = stringResource(R.string.details_author_badge),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (proposal.canLike) {
                    ActivityRating(
                        likesCount = proposal.likesCount,
                        likedByMe = proposal.likedByMe,
                        dislikedByMe = proposal.dislikedByMe,
                        enabled = canInteract && proposal.canLike,
                        onLike = onLike,
                        onDislike = onDislike,
                        onUnlike = onUnlike,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                    )
                }

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
