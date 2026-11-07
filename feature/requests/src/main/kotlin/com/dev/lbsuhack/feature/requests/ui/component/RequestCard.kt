package com.dev.lbsuhack.feature.requests.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackBadge
import com.dev.lbsuhack.core.ui.component.HackDetailsLink
import com.dev.lbsuhack.core.ui.component.HackTag
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.domain.isPending
import com.dev.lbsuhack.feature.requests.ui.labels.categoryIcon
import com.dev.lbsuhack.feature.requests.ui.labels.categoryLabelRes

@Composable
internal fun RequestCard(
    request: ReviewRequest,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showsVotes = request.audience == RequestAudience.ADMIN || request.status == ReviewStatus.APPROVED
    val probability = request.successProbability
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onOpen),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .alpha(if (request.status == ReviewStatus.DECLINED) DECLINED_ALPHA else 1f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HackTag(
                    text = stringResource(categoryLabelRes(request.categoryKey)),
                    icon = categoryIcon(request.categoryKey),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                RequestStatusChip(
                    status = request.status,
                    modifier = Modifier.weight(1.3f).fillMaxHeight(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = request.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (request.originKey == ORIGIN_SCHOOL) {
                HackBadge(text = stringResource(R.string.requests_origin_school))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = request.authorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (request.originKey == ORIGIN_TEACHER) {
                        Spacer(modifier = Modifier.width(8.dp))
                        HackBadge(text = stringResource(R.string.requests_origin_teacher))
                    }
                }
            }

            if (request.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (request.isPending) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            request.proposedDate?.let { date ->
                Spacer(modifier = Modifier.height(14.dp))
                RequestDateRow(date = date)
            }

            Spacer(modifier = Modifier.height(16.dp))
            RequestStageNote(request = request)

            if (showsVotes) {
                Spacer(modifier = Modifier.height(12.dp))
                RequestVotes(request = request)
            }

            if (request.isPending && probability != null) {
                Spacer(modifier = Modifier.height(14.dp))
                RequestChanceMeter(probability = probability, modifier = Modifier.padding(horizontal = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (request.isPending) {
                RequestActions(onApprove = onApprove, onDecline = onDecline)
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    HackDetailsLink(
                        text = stringResource(R.string.requests_open_details),
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

private const val ORIGIN_TEACHER = "TEACHER"
private const val ORIGIN_SCHOOL = "ADMIN"
private const val DECLINED_ALPHA = 0.62f
