package com.dev.lbsuhack.feature.profile.ui.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.ui.component.HackSectionLabel
import com.dev.lbsuhack.feature.profile.R

@Composable
fun ProfileStaffSection(
    role: UserRole,
    onOpenRequests: (UserRole) -> Unit,
    onOpenActivities: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTeacher = role == UserRole.TEACHER
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HackSectionLabel(
            text = stringResource(R.string.profile_requests),
            modifier = Modifier.padding(start = 4.dp),
        )
        ProfileNavigationCard(
            icon = if (isTeacher) Icons.AutoMirrored.Filled.FactCheck else Icons.Outlined.Verified,
            title = stringResource(if (isTeacher) R.string.profile_requests_teacher else R.string.profile_requests_admin),
            onClick = { onOpenRequests(role) },
        )
        ProfileNavigationCard(
            icon = if (isTeacher) Icons.Outlined.Event else Icons.Outlined.AccountBalance,
            title = stringResource(if (isTeacher) R.string.profile_my_activities else R.string.profile_school_activities),
            onClick = onOpenActivities,
        )
    }
}
