package com.dev.lbsuhack.feature.activities.ui.details.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackInfoButton
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityOrigin
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

@Composable
internal fun ActivityStatusNotice(proposal: ActivityProposal, modifier: Modifier = Modifier) {
    val icon: ImageVector
    val titleRes: Int
    val descriptionRes: Int?

    when (proposal.status) {
        ActivityStatus.ON_MODERATION -> {
            icon = Icons.Outlined.HourglassEmpty
            titleRes = R.string.activities_status_moderating
            descriptionRes = R.string.details_moderating_desc
        }
        ActivityStatus.ON_VOTE -> {
            icon = Icons.Outlined.Groups
            titleRes = R.string.details_recruiting_title
            descriptionRes = if (proposal.origin == ActivityOrigin.SCHOOL) {
                R.string.details_school_recruiting_desc
            } else {
                R.string.details_recruiting_desc
            }
        }
        ActivityStatus.ON_ADMIN_MODERATION -> {
            icon = Icons.Outlined.HourglassEmpty
            titleRes = R.string.details_admin_pending_title
            descriptionRes = R.string.details_admin_pending_desc
        }
        ActivityStatus.FINALIZED -> {
            icon = Icons.Default.CheckCircle
            titleRes = R.string.details_admin_approved_title
            descriptionRes = null
        }
        ActivityStatus.DECLINED,
        ActivityStatus.DECLINED_BY_ADMIN -> {
            icon = Icons.Default.Cancel
            titleRes = R.string.details_rejected_title
            descriptionRes = R.string.details_rejected_desc
        }
        ActivityStatus.EXPIRED -> {
            icon = Icons.Default.Cancel
            titleRes = R.string.details_expired_title
            descriptionRes = R.string.details_expired_desc
        }
    }

    val deadlineLabel = when (proposal.status) {
        ActivityStatus.ON_MODERATION -> R.string.details_deadline_label_moderation
        ActivityStatus.ON_VOTE -> R.string.details_deadline_label_voting
        ActivityStatus.ON_ADMIN_MODERATION -> R.string.details_deadline_label_admin
        else -> null
    }
    val deadline = proposal.deadlineTime?.takeIf { it != proposal.confirmedTime }
    val infoText = listOfNotNull(
        descriptionRes?.let { stringResource(it) },
        if (deadlineLabel != null && deadline != null) "${stringResource(deadlineLabel)}: $deadline" else null,
    ).joinToString("\n\n")

    DetailsCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(titleRes),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (infoText.isNotBlank()) {
                HackInfoButton(title = stringResource(titleRes), text = infoText)
            }
        }
        proposal.declineReason?.takeIf { it.isNotBlank() }?.let { reason ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        proposal.confirmedTime?.let { date ->
            Spacer(modifier = Modifier.height(16.dp))
            ConfirmedDetailsRow(
                icon = Icons.Outlined.Event,
                label = stringResource(R.string.details_confirmed_time_label),
                value = date,
                accent = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
