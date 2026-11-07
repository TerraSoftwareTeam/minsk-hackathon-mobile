package com.dev.lbsuhack.feature.requests.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackStatusChip
import com.dev.lbsuhack.core.ui.component.HackStatusEmphasis
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.ui.labels.labelRes

@Composable
internal fun RequestStatusChip(status: ReviewStatus, modifier: Modifier = Modifier) {
    HackStatusChip(
        text = stringResource(status.labelRes()),
        icon = when (status) {
            ReviewStatus.PENDING_TEACHER -> Icons.Outlined.HourglassEmpty
            ReviewStatus.PENDING_ADMIN -> Icons.Outlined.AccountBalance
            ReviewStatus.APPROVED -> Icons.Outlined.CheckCircle
            ReviewStatus.DECLINED -> Icons.Outlined.Block
        },
        emphasis = when (status) {
            ReviewStatus.PENDING_TEACHER,
            ReviewStatus.PENDING_ADMIN,
            -> HackStatusEmphasis.ACTIVE
            ReviewStatus.APPROVED -> HackStatusEmphasis.DONE
            ReviewStatus.DECLINED -> HackStatusEmphasis.CLOSED
        },
        modifier = modifier,
    )
}
