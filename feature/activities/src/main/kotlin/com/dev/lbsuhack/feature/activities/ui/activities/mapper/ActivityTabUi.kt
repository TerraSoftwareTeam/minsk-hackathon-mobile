package com.dev.lbsuhack.feature.activities.ui.activities.mapper

import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab

internal fun ActivityTab.labelRes(): Int = when (this) {
    ActivityTab.ALL -> R.string.activities_tab_all
    ActivityTab.ON_VOTE -> R.string.activities_tab_recruiting
    ActivityTab.ON_ADMIN_MODERATION -> R.string.activities_tab_pending_admin
    ActivityTab.FINALIZED -> R.string.activities_tab_approved
    ActivityTab.MY_PROPOSALS -> R.string.activities_tab_my_proposals
    ActivityTab.MY_VOTES -> R.string.activities_tab_my_participations
}
