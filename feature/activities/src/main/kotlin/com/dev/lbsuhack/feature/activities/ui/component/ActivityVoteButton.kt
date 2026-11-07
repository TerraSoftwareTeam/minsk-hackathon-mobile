package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal

@Composable
internal fun ActivityVoteButton(
    proposal: ActivityProposal,
    isAuthor: Boolean,
    canInteract: Boolean,
    onVote: () -> Unit,
    onUnvote: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDetails: (() -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.medium
    when {
        isAuthor -> {
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = modifier,
                shape = shape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = stringResource(R.string.activities_author_cannot_vote),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        proposal.hasVoted -> {
            val canCancel = proposal.canChangeVote && canInteract
            OutlinedButton(
                onClick = onUnvote,
                enabled = canCancel,
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                shape = shape,
                border = BorderStroke(
                    1.dp,
                    if (canCancel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = stringResource(R.string.activities_leave_action),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (canCancel) {
                        Text(
                            text = stringResource(R.string.activities_unvote_action),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        canInteract && proposal.canChangeVote -> {
            Button(
                onClick = onVote,
                modifier = modifier,
                shape = shape,
            ) {
                Text(
                    text = stringResource(R.string.activities_join_action),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        onOpenDetails != null -> {
            OutlinedButton(
                onClick = onOpenDetails,
                modifier = modifier,
                shape = shape,
            ) {
                Text(
                    text = stringResource(R.string.activities_open_details),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        else -> {
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = modifier,
                shape = shape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = stringResource(R.string.activities_vote_unavailable),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}
