package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackStatusChip
import com.dev.lbsuhack.core.ui.component.HackStatusEmphasis
import com.dev.lbsuhack.feature.activities.domain.ActivityStage
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.domain.stage
import com.dev.lbsuhack.feature.activities.ui.activities.mapper.labelRes

@Composable
internal fun ActivityStatusChip(status: ActivityStatus, modifier: Modifier = Modifier) {
    HackStatusChip(
        text = stringResource(status.labelRes()),
        icon = status.icon(),
        emphasis = when (status.stage) {
            ActivityStage.VOTING -> HackStatusEmphasis.ACTIVE
            ActivityStage.WAITING -> HackStatusEmphasis.WAITING
            ActivityStage.CONFIRMED -> HackStatusEmphasis.DONE
            ActivityStage.CLOSED -> HackStatusEmphasis.CLOSED
        },
        modifier = modifier,
    )
}

private fun ActivityStatus.icon(): ImageVector = when (this) {
    ActivityStatus.ON_MODERATION -> Icons.Outlined.HourglassEmpty
    ActivityStatus.ON_VOTE -> Icons.Outlined.HowToVote
    ActivityStatus.ON_ADMIN_MODERATION -> Icons.Outlined.AccountBalance
    ActivityStatus.FINALIZED -> Icons.Outlined.CheckCircle
    ActivityStatus.DECLINED,
    ActivityStatus.DECLINED_BY_ADMIN,
    -> Icons.Outlined.Block
    ActivityStatus.EXPIRED -> Icons.Outlined.EventBusy
}
