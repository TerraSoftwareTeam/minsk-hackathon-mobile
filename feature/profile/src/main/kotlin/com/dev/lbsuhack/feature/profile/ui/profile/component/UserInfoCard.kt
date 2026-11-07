package com.dev.lbsuhack.feature.profile.ui.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.ui.component.HackAvatar
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.ui.common.ProfileCard

@Composable
fun UserInfoCard(
    fullName: String,
    pupilClass: String,
    schoolName: String,
    role: UserRole,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HackAvatar(
                name = fullName,
                onClick = onEditClick,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = fullName.ifEmpty { stringResource(R.string.profile_name_unknown) },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.profile_edit),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        val hasExtraInfo = role == UserRole.PUPIL || role == UserRole.TEACHER
        if (hasExtraInfo) {
            val resolvedSchool = schoolName.ifBlank { stringResource(R.string.profile_school_not_specified) }
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (role == UserRole.PUPIL && pupilClass.isNotBlank()) {
                    ProfileInfoRow(icon = Icons.Outlined.Groups, text = pupilClass)
                }
                ProfileInfoRow(icon = Icons.Outlined.School, text = resolvedSchool)
            }
        }
    }
}
