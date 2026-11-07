package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab
import com.dev.lbsuhack.feature.requests.ui.component.RequestCard
import com.dev.lbsuhack.feature.requests.ui.labels.subtitleLabelRes

@Composable
internal fun RequestsContent(
    audience: RequestAudience,
    state: RequestsStore.State,
    modifier: Modifier = Modifier,
    onSelectTab: (RequestsTab) -> Unit,
    onApprove: (String) -> Unit,
    onDecline: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(audience.subtitleLabelRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp),
        )

        RequestsTabs(
            selectedTab = state.selectedTab,
            pendingCount = state.pendingCount,
            onSelectTab = onSelectTab,
        )

        HorizontalDivider(color = MaterialTheme.hack.gridLine)

        if (state.visibleRequests.isEmpty()) {
            RequestsEmptyState(
                tab = state.selectedTab,
                audience = audience,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = state.visibleRequests,
                    key = { it.id },
                ) { request ->
                    RequestCard(
                        request = request,
                        onApprove = { onApprove(request.id) },
                        onDecline = { onDecline(request.id) },
                    )
                }
            }
        }
    }
}
