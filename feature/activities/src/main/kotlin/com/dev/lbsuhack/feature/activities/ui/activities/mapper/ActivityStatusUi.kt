package com.dev.lbsuhack.feature.activities.ui.activities.mapper

import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

internal fun ActivityStatus.labelRes(): Int = when (this) {
    ActivityStatus.ON_MODERATION -> R.string.activities_status_moderating
    ActivityStatus.ON_VOTE -> R.string.activities_status_recruiting
    ActivityStatus.ON_ADMIN_MODERATION -> R.string.activities_status_pending_admin
    ActivityStatus.FINALIZED -> R.string.activities_status_approved
    ActivityStatus.EXPIRED -> R.string.activities_status_expired
    ActivityStatus.DECLINED,
    ActivityStatus.DECLINED_BY_ADMIN -> R.string.activities_status_rejected
}
