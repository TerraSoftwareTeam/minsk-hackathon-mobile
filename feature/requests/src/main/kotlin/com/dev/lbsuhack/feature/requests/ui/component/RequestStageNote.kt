package com.dev.lbsuhack.feature.requests.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackStageNote
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.ui.labels.rememberRequestDateFormatter

@Composable
internal fun RequestStageNote(request: ReviewRequest, modifier: Modifier = Modifier) {
    val formatDate = rememberRequestDateFormatter()
    when (request.status) {
        ReviewStatus.PENDING_TEACHER -> HackStageNote(
            icon = Icons.Outlined.HourglassEmpty,
            title = stringResource(R.string.requests_note_teacher_pending_title),
            subtitle = request.deadline
                ?.let { stringResource(R.string.requests_note_teacher_pending_until_format, formatDate(it)) }
                ?: stringResource(R.string.requests_note_teacher_pending_desc),
            modifier = modifier,
        )
        ReviewStatus.PENDING_ADMIN -> HackStageNote(
            icon = Icons.Outlined.AccountBalance,
            title = stringResource(R.string.requests_note_admin_pending_title),
            subtitle = request.deadline
                ?.let { stringResource(R.string.requests_note_admin_pending_until_format, formatDate(it)) }
                ?: stringResource(R.string.requests_note_admin_pending_desc),
            modifier = modifier,
        )
        ReviewStatus.APPROVED -> HackStageNote(
            icon = Icons.Outlined.CheckCircle,
            title = stringResource(
                if (request.audience == RequestAudience.ADMIN) {
                    R.string.requests_note_admin_approved_title
                } else {
                    R.string.requests_note_teacher_approved_title
                },
            ),
            subtitle = if (request.audience == RequestAudience.ADMIN) {
                stringResource(R.string.requests_note_admin_approved_desc)
            } else {
                stringResource(R.string.requests_note_teacher_approved_desc)
            },
            modifier = modifier,
        )
        ReviewStatus.DECLINED -> HackStageNote(
            icon = Icons.Outlined.Block,
            title = stringResource(R.string.requests_note_declined_title),
            subtitle = request.declineReason?.takeIf { it.isNotBlank() }
                ?.let { stringResource(R.string.requests_decline_reason_format, it) }
                ?: stringResource(R.string.requests_note_no_reason),
            modifier = modifier,
        )
    }
}
