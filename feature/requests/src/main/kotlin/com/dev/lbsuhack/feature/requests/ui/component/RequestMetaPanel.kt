package com.dev.lbsuhack.feature.requests.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.ui.labels.rememberRequestDateFormatter

@Composable
internal fun RequestMetaPanel(
    request: ReviewRequest,
    modifier: Modifier = Modifier,
) {
    val formatDate = rememberRequestDateFormatter()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.large,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val proposedDate = request.proposedDate
        if (proposedDate != null) {
            RequestInfoRow(
                icon = Icons.Outlined.Schedule,
                text = stringResource(R.string.requests_date_format, formatDate(proposedDate)),
            )
        } else {
            RequestInfoRow(
                icon = Icons.Outlined.Schedule,
                text = stringResource(R.string.requests_date_not_set),
            )
        }
        if (request.showsVotingResults) {
            RequestInfoRow(
                icon = Icons.Outlined.HowToVote,
                text = stringResource(R.string.requests_votes_format, request.votesCount, request.voteThreshold),
            )
            RequestInfoRow(
                icon = Icons.Outlined.ThumbUp,
                text = stringResource(R.string.requests_rating_format, request.likesCount),
            )
            if (request.voters.isNotEmpty()) {
                RequestInfoRow(
                    icon = Icons.Outlined.Groups,
                    text = stringResource(R.string.requests_voters_format, request.voters.joinToString(", ")),
                )
            }
        }
    }
}

private val ReviewRequest.showsVotingResults: Boolean
    get() = audience == RequestAudience.ADMIN || status == ReviewStatus.APPROVED
