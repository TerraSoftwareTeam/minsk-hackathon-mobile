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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.activities.presentation.ActivityRatingMode
import com.dev.lbsuhack.feature.activities.presentation.actionsFor
import com.dev.lbsuhack.feature.activities.ui.component.ActivityRatingSummary
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStage
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.domain.stage
import com.dev.lbsuhack.feature.activities.ui.component.ActivityAuthorRow
import com.dev.lbsuhack.feature.activities.ui.component.ActivityChanceMeter
import com.dev.lbsuhack.feature.activities.ui.component.ActivityRating
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVoteButton

@Composable
internal fun ActivityCard(
    proposal: ActivityProposal,
    isAuthor: Boolean,
    viewerRole: UserRole?,
    canInteract: Boolean,
    canLike: Boolean,
    onClick: () -> Unit,
    onVote: () -> Unit,
    onUnvote: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onUnlike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stage = proposal.stage
    val actions = proposal.actionsFor(viewerRole)
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
            ActivityCardCover(proposal = proposal, photoUri = photoUri, isAuthor = isAuthor)
        }
        Column(
            modifier = Modifier
                .padding(20.dp)
                .alpha(if (stage == ActivityStage.CLOSED) CLOSED_ALPHA else 1f),
        ) {
            if (proposal.photoUri == null) {
                ActivityMetadata(proposal = proposal, isAuthor = isAuthor)
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

            ActivityAuthorRow(proposal = proposal)

            if (proposal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = proposal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (stage == ActivityStage.VOTING) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            proposal.confirmedTime?.let { date ->
                Spacer(modifier = Modifier.height(16.dp))
                ConfirmedActivityInfo(confirmedTime = date)
            }

            when (stage) {
                ActivityStage.VOTING,
                ActivityStage.CONFIRMED,
                -> {
                    Spacer(modifier = Modifier.height(18.dp))
                    ActivityVoteProgress(proposal = proposal)
                }
                ActivityStage.WAITING,
                ActivityStage.CLOSED,
                -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    ActivityStageNote(proposal = proposal)
                }
            }

            if (proposal.showsChance && proposal.status != ActivityStatus.ON_MODERATION) {
                Spacer(modifier = Modifier.height(14.dp))
                ActivityChanceMeter(
                    probability = proposal.successProbability,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            if (viewerRole == UserRole.PUPIL) {
                Spacer(modifier = Modifier.height(16.dp))

                when (stage) {
                    ActivityStage.VOTING,
                    ActivityStage.CONFIRMED,
                    -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ActivityRating(
                            likesCount = proposal.likesCount,
                            likedByMe = proposal.likedByMe,
                            dislikedByMe = proposal.dislikedByMe,
                            enabled = canLike,
                            onLike = onLike,
                            onDislike = onDislike,
                            onUnlike = onUnlike,
                        )
                        ActivityVoteButton(
                            proposal = proposal,
                            canInteract = canInteract,
                            onVote = onVote,
                            onUnvote = onUnvote,
                            onOpenDetails = onClick,
                            modifier = Modifier.weight(1f).height(50.dp),
                        )
                    }
                    ActivityStage.WAITING,
                    ActivityStage.CLOSED,
                    -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (proposal.canLike) {
                            ActivityRating(
                                likesCount = proposal.likesCount,
                                likedByMe = proposal.likedByMe,
                                dislikedByMe = proposal.dislikedByMe,
                                enabled = canLike,
                                onLike = onLike,
                                onDislike = onDislike,
                                onUnlike = onUnlike,
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        ActivityDetailsLink(modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp))
                    }
                }
            } else if (actions.ratingMode == ActivityRatingMode.SUMMARY) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    ActivityRatingSummary(score = proposal.likesCount)
                }
            }
        }
    }
}

private const val CLOSED_ALPHA = 0.62f
