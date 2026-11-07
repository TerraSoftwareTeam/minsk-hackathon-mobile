package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.domain.ActivityOrigin
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.ui.component.ActivityAuthorBadges
import com.dev.lbsuhack.feature.activities.ui.component.ActivityChanceMeter
import com.dev.lbsuhack.feature.activities.ui.component.ActivityRating
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVoteButton

@Composable
internal fun ActivityCard(
    proposal: ActivityProposal,
    isAuthor: Boolean,
    canInteract: Boolean,
    canLike: Boolean,
    onClick: () -> Unit,
    onVote: () -> Unit,
    onUnvote: () -> Unit,
    onLike: () -> Unit,
    onUnlike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge,
            )
            .clickable(onClick = onClick),
    ) {
        proposal.photoUri?.let { photoUri ->
            ActivityCardCover(proposal = proposal, photoUri = photoUri)
        }
        Column(modifier = Modifier.padding(20.dp)) {
            if (proposal.photoUri == null) {
                ActivityMetadata(proposal = proposal)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = proposal.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = proposal.authorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ActivityAuthorBadges(proposal = proposal, isAuthor = isAuthor)
            }

            if (proposal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = proposal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (proposal.confirmedTime != null && (proposal.status == ActivityStatus.FINALIZED || proposal.origin == ActivityOrigin.SCHOOL)) {
                Spacer(modifier = Modifier.height(16.dp))
                ConfirmedActivityInfo(confirmedTime = proposal.confirmedTime)
            }

            Spacer(modifier = Modifier.height(18.dp))

            ActivityVoteProgress(proposal = proposal)

            if (proposal.showsChance) {
                Spacer(modifier = Modifier.height(14.dp))
                ActivityChanceMeter(
                    probability = proposal.successProbability,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActivityRating(
                    likesCount = proposal.likesCount,
                    likedByMe = proposal.likedByMe,
                    enabled = canLike,
                    onLike = onLike,
                    onUnlike = onUnlike,
                )
                ActivityVoteButton(
                    proposal = proposal,
                    isAuthor = isAuthor,
                    canInteract = canInteract,
                    onVote = onVote,
                    onUnvote = onUnvote,
                    onOpenDetails = onClick,
                    modifier = Modifier.weight(1f).height(50.dp),
                )
            }
        }
    }
}
