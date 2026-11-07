package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackStageNote
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

@Composable
internal fun ActivityStageNote(proposal: ActivityProposal, modifier: Modifier = Modifier) {
    val note = proposal.stageNote() ?: return
    val subtitle = when (proposal.status) {
        ActivityStatus.DECLINED,
        ActivityStatus.DECLINED_BY_ADMIN,
        -> proposal.declineReason?.takeIf { it.isNotBlank() }
            ?.let { stringResource(R.string.activities_note_reason_format, it) }
            ?: stringResource(R.string.activities_note_no_reason)
        ActivityStatus.ON_MODERATION -> proposal.deadlineTime
            ?.let { stringResource(R.string.activities_note_moderation_until_format, it) }
            ?: stringResource(note.subtitleRes)
        ActivityStatus.ON_ADMIN_MODERATION -> proposal.deadlineTime
            ?.let { stringResource(R.string.activities_note_admin_until_format, it) }
            ?: stringResource(note.subtitleRes)
        ActivityStatus.EXPIRED -> if (proposal.missedMinimum) {
            stringResource(R.string.activities_note_expired_votes_format, proposal.votesCount, proposal.voteThreshold)
        } else {
            stringResource(note.subtitleRes)
        }
        else -> stringResource(note.subtitleRes)
    }
    HackStageNote(
        icon = note.icon,
        title = stringResource(note.titleRes),
        subtitle = subtitle,
        modifier = modifier,
    )
}

private class StageNote(val icon: ImageVector, val titleRes: Int, val subtitleRes: Int)

private fun ActivityProposal.stageNote(): StageNote? = when (status) {
    ActivityStatus.ON_MODERATION -> StageNote(
        Icons.Outlined.HourglassEmpty,
        R.string.activities_note_moderation_title,
        R.string.activities_note_moderation_desc,
    )
    ActivityStatus.ON_ADMIN_MODERATION -> StageNote(
        Icons.Outlined.AccountBalance,
        R.string.activities_note_admin_title,
        R.string.activities_note_admin_desc,
    )
    ActivityStatus.DECLINED -> StageNote(
        Icons.Outlined.Block,
        R.string.activities_note_declined_teacher_title,
        R.string.activities_note_no_reason,
    )
    ActivityStatus.DECLINED_BY_ADMIN -> StageNote(
        Icons.Outlined.Block,
        R.string.activities_note_declined_admin_title,
        R.string.activities_note_no_reason,
    )
    ActivityStatus.EXPIRED -> StageNote(
        Icons.Outlined.EventBusy,
        R.string.activities_note_expired_title,
        R.string.activities_note_expired_desc,
    )
    ActivityStatus.ON_VOTE,
    ActivityStatus.FINALIZED,
    -> null
}
