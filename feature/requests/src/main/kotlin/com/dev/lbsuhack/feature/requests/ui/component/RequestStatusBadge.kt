package com.dev.lbsuhack.feature.requests.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.ui.labels.labelRes

@Composable
internal fun RequestStatusBadge(status: ReviewStatus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(status.statusColor(), CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(status.labelRes()),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReviewStatus.statusColor(): Color = when (this) {
    ReviewStatus.PENDING_TEACHER -> MaterialTheme.colorScheme.primary
    ReviewStatus.PENDING_ADMIN -> MaterialTheme.colorScheme.onSurfaceVariant
    ReviewStatus.APPROVED -> MaterialTheme.hack.success
    ReviewStatus.DECLINED -> MaterialTheme.hack.danger
}
