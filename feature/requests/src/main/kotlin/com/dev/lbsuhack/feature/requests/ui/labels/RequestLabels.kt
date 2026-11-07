package com.dev.lbsuhack.feature.requests.ui.labels

import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab

internal fun ReviewStatus.labelRes(): Int = when (this) {
    ReviewStatus.PENDING_TEACHER -> R.string.requests_status_pending_teacher
    ReviewStatus.PENDING_ADMIN -> R.string.requests_status_pending_admin
    ReviewStatus.APPROVED -> R.string.requests_status_approved
    ReviewStatus.DECLINED -> R.string.requests_status_declined
}

internal fun RequestsTab.labelRes(): Int = when (this) {
    RequestsTab.PENDING -> R.string.requests_tab_pending
    RequestsTab.PROCESSED -> R.string.requests_tab_processed
    RequestsTab.ALL -> R.string.requests_tab_all
}
