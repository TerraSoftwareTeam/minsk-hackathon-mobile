package com.dev.lbsuhack.feature.profile.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStore
import com.dev.lbsuhack.feature.profile.ui.common.ProfileCard
import com.dev.lbsuhack.feature.profile.ui.profile.component.NotificationsCard
import com.dev.lbsuhack.feature.profile.ui.profile.component.ProfileActionRow
import com.dev.lbsuhack.feature.profile.ui.profile.component.ProfileStaffSection
import com.dev.lbsuhack.feature.profile.ui.profile.component.UserInfoCard

@Composable
fun ProfileContent(
    state: ProfileStore.State,
    onEditClick: () -> Unit,
    onOpenRequests: (UserRole) -> Unit,
    onOpenActivities: () -> Unit,
    onToggleNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UserInfoCard(
            fullName = state.fullName,
            pupilClass = state.pupilClass,
            schoolName = state.schoolName,
            role = state.role,
            onEditClick = onEditClick,
        )

        if (state.canReviewRequests) {
            ProfileStaffSection(
                role = state.role,
                onOpenRequests = onOpenRequests,
                onOpenActivities = onOpenActivities,
            )
        }

        NotificationsCard(
            title = stringResource(R.string.profile_notifications),
            enabled = state.notificationsEnabled,
            onToggle = { onToggleNotifications() },
        )

        ProfileCard(contentPadding = 0.dp) {
            ProfileActionRow(
                icon = Icons.Outlined.Settings,
                text = stringResource(R.string.profile_open_settings),
                onClick = onOpenSettings,
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            ProfileActionRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                text = stringResource(R.string.profile_logout),
                showChevron = false,
                onClick = onLogoutClick,
            )
        }
    }
}
