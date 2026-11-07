package com.dev.lbsuhack.feature.profile.ui.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.ui.component.HackSectionLabel
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.ui.common.ProfileCard

@Composable
fun ProfileRequestsSection(
    role: UserRole,
    onOpenRequests: (UserRole) -> Unit,
    onCreateActivity: () -> Unit,
    onOpenActivities: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HackSectionLabel(
            text = stringResource(R.string.profile_requests),
            modifier = Modifier.padding(start = 4.dp),
        )
        ProfileCard(contentPadding = 0.dp) {
            ProfileActionRow(
                icon = if (role == UserRole.TEACHER) Icons.AutoMirrored.Filled.FactCheck else Icons.Outlined.Verified,
                text = stringResource(
                    if (role == UserRole.TEACHER) R.string.profile_requests_teacher else R.string.profile_requests_admin,
                ),
                onClick = { onOpenRequests(role) },
            )
            SectionDivider()
            ProfileActionRow(
                icon = Icons.Outlined.AddCircleOutline,
                text = stringResource(R.string.profile_create_activity),
                onClick = onCreateActivity,
            )
            SectionDivider()
            ProfileActionRow(
                icon = Icons.Outlined.EventNote,
                text = stringResource(R.string.profile_my_activities),
                onClick = onOpenActivities,
            )
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}
