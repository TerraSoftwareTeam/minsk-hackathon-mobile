package com.dev.lbsuhack.feature.requests.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.isPending

@Composable
internal fun RequestCard(
    request: ReviewRequest,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge,
            )
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RequestKindBadge(
                audience = request.audience,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(10.dp))
            RequestStatusBadge(status = request.status)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = request.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(10.dp))

        RequestInfoRow(
            icon = Icons.Outlined.Person,
            text = stringResource(R.string.requests_author_name_format, request.authorName),
        )

        if (request.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RequestMetaPanel(request = request)

        val probability = request.successProbability
        if (request.isPending && probability != null) {
            Spacer(modifier = Modifier.height(14.dp))
            RequestChanceMeter(probability = probability, modifier = Modifier.padding(horizontal = 4.dp))
        }

        request.declineReason?.let { reason ->
            RequestDeclineReason(reason = reason)
        }

        if (request.isPending) {
            Spacer(modifier = Modifier.height(16.dp))
            RequestActions(
                onApprove = onApprove,
                onDecline = onDecline,
            )
        }
    }
}
