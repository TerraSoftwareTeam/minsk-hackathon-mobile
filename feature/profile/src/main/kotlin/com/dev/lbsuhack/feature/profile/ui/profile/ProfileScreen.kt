package com.dev.lbsuhack.feature.profile.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.ui.component.HackAvatar
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.presentation.profile.ProfileViewModel
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStore
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRequests: (UserRole) -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isLogoutDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.accept(ProfileStore.Intent.Load)
    }

    if (isLogoutDialogVisible) {
        LogoutConfirmDialog(
            onConfirm = {
                isLogoutDialogVisible = false
                viewModel.accept(ProfileStore.Intent.Logout)
            },
            onDismiss = { isLogoutDialogVisible = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileHeader(
                name = state.name,
                pupilClass = state.pupilClass,
                onBack = onBack,
            )

            UserInfoCard(
                name = state.name,
                pupilClass = state.pupilClass,
                school = state.schoolName,
            )

            ProfileRequestsSection(onOpenRequests = onOpenRequests)

            NotificationsCard(
                title = stringResource(R.string.profile_notifications),
                description = stringResource(R.string.profile_notifications_description),
                enabled = state.notificationsEnabled,
                onToggle = { viewModel.accept(ProfileStore.Intent.ToggleNotifications) },
            )

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

            FilledTonalButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_open_settings))
            }

            OutlinedButton(
                onClick = { isLogoutDialogVisible = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_logout))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    pupilClass: String,
    onBack: () -> Unit,
) {
    Column {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.profile_back),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${name.ifEmpty { "Никита Иванов" }} · ${pupilClass.ifEmpty { "11 «М»" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            HackAvatar(name = name.ifEmpty { "Никита Иванов" })
        }
    }
}

@Composable
private fun UserInfoCard(
    name: String,
    pupilClass: String,
    school: String,
) {
    ProfileCard {
        Text(
            text = name.ifEmpty { "Никита Иванов" },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoChip(
                text = pupilClass.ifEmpty { "11 «М»" },
                color = MaterialTheme.colorScheme.primary,
            )
            if (school.isNotBlank()) {
                InfoChip(
                    text = school,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }
}

@Composable
private fun ProfileRequestsSection(
    onOpenRequests: (UserRole) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.profile_requests),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { onOpenRequests(UserRole.TEACHER) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FactCheck,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_requests_teacher), maxLines = 1)
            }

            OutlinedButton(
                onClick = { onOpenRequests(UserRole.ORGANIZATOR) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_requests_admin), maxLines = 1)
            }
        }
    }
}

@Composable
private fun NotificationsCard(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ProfileCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = Icons.Outlined.Notifications)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun SavedCard(
    count: Int,
    title: String,
    countText: String,
    emptyText: String,
) {
    ProfileCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = Icons.Outlined.BookmarkBorder)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (count == 0) emptyText else countText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (count > 0) {
                InfoChip(
                    text = count.toString(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
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
        content = content,
    )
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.shapes.medium,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(color.copy(alpha = 0.14f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.profile_logout_confirm_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Text(
                text = stringResource(R.string.profile_logout_confirm_desc),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(stringResource(R.string.profile_logout))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_cancel))
            }
        },
    )
}
