package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackSectionHeader
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.store.requests.RequestsSectionKind
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab
import com.dev.lbsuhack.feature.requests.ui.component.RequestCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RequestsContent(
    audience: RequestAudience,
    state: RequestsStore.State,
    onApprove: (String) -> Unit,
    onDecline: (String) -> Unit,
    onOpen: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        val sections = state.queueSections
        if (sections.isEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item(key = EMPTY_STATE_KEY) {
                    RequestsEmptyState(
                        tab = RequestsTab.PENDING,
                        audience = audience,
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 48.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                sections.forEach { section ->
                    item(key = "section-${section.kind.name}") {
                        HackSectionHeader(
                            title = stringResource(
                                when (section.kind) {
                                    RequestsSectionKind.PENDING -> R.string.requests_section_pending
                                    RequestsSectionKind.PROCESSED -> R.string.requests_section_processed
                                },
                            ),
                            count = section.requests.size,
                            modifier = Modifier.animateItem(),
                        )
                    }
                    items(
                        items = section.requests,
                        key = { it.id },
                    ) { request ->
                        RequestCard(
                            request = request,
                            actionsEnabled = !state.isSubmitting,
                            isApproving = state.approvingRequestId == request.id,
                            onApprove = { onApprove(request.id) },
                            onDecline = { onDecline(request.id) },
                            onOpen = { onOpen(request.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

private const val EMPTY_STATE_KEY = "requests-empty"
