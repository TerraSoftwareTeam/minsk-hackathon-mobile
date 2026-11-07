package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackEmptyState
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab

@Composable
internal fun RequestsEmptyState(
    tab: RequestsTab,
    audience: RequestAudience,
    modifier: Modifier = Modifier,
) {
    HackEmptyState(
        title = stringResource(R.string.requests_empty_title),
        description = stringResource(
            when (tab) {
                RequestsTab.PENDING -> when (audience) {
                    RequestAudience.TEACHER -> R.string.requests_empty_pending_teacher
                    RequestAudience.ADMIN -> R.string.requests_empty_pending_admin
                }
                RequestsTab.PROCESSED -> R.string.requests_empty_processed
                RequestsTab.ALL -> R.string.requests_empty_all
            },
        ),
        icon = Icons.Outlined.Inbox,
        modifier = modifier,
    )
}
