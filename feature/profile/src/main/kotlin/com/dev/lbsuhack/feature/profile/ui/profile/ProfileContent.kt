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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStore
import com.dev.lbsuhack.feature.profile.ui.common.ProfileCard
import com.dev.lbsuhack.feature.profile.ui.profile.component.NotificationsCard
import com.dev.lbsuhack.feature.profile.ui.profile.component.ProfileActionRow
import com.dev.lbsuhack.feature.profile.ui.profile.component.ProfileRequestsSection
import com.dev.lbsuhack.feature.profile.ui.profile.component.SavedCard
import com.dev.lbsuhack.feature.profile.ui.profile.component.UserInfoCard

@Composable
fun ProfileContent(
    state: ProfileStore.State,
    onEditClick: () -> Unit,
    onOpenRequests: (UserRole) -> Unit,
    onToggleNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
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
            ProfileRequestsSection(
                role = state.role,
                onOpenRequests = onOpenRequests,
            )
        }

        NotificationsCard(
            title = stringResource(R.string.profile_notifications),
            description = stringResource(R.string.profile_notifications_description),
            enabled = state.notificationsEnabled,
            onToggle = { onToggleNotifications() },
        )

        if (state.role == UserRole.PUPIL) {
            SavedCard(
                count = state.savedCount,
                title = stringResource(R.string.profile_saved_title),
                countText = pluralStringResource(
                    R.plurals.profile_saved_items,
                    state.savedCount,
                    state.savedCount,
                ),
                emptyText = stringResource(R.string.profile_saved_empty),
            )
        }

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
