package com.dev.lbsuhack.feature.profile.ui.profile.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.profile.ui.common.ProfileCard
import com.dev.lbsuhack.feature.profile.ui.common.ProfileIconBadge

@Composable
fun NotificationsCard(
    title: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileCard(modifier = modifier, contentPadding = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = enabled,
                    role = Role.Switch,
                    onValueChange = onToggle,
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileIconBadge(icon = Icons.Outlined.Notifications)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
                checked = enabled,
                onCheckedChange = null,
            )
        }
    }
}
