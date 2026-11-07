package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.core.ui.component.HackHomeHeader
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab

@Composable
internal fun ActivitiesTopBar(
    userName: String?,
    pupilClass: String?,
    selectedCategory: ActivityCategory?,
    selectedTab: ActivityTab,
    onSelectCategory: (ActivityCategory?) -> Unit,
    onSelectTab: (ActivityTab) -> Unit,
    onOpenProfile: () -> Unit,
    showQuickFilters: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(top = 16.dp),
    ) {
        HackHomeHeader(
            title = stringResource(R.string.activities_title),
            subtitle = if (userName != null) {
                if (!pupilClass.isNullOrBlank()) {
                    stringResource(R.string.activities_greeting_format, userName, pupilClass)
                } else {
                    stringResource(R.string.activities_greeting_simple_format, userName)
                }
            } else {
                stringResource(R.string.activities_subtitle)
            },
            avatarName = userName.orEmpty(),
            onAvatarClick = onOpenProfile,
        )

        Spacer(modifier = Modifier.height(20.dp))

        ActivitiesFilters(
            selectedCategory = selectedCategory,
            selectedTab = selectedTab,
            showQuickFilters = showQuickFilters,
            onSelectCategory = onSelectCategory,
            onSelectTab = onSelectTab,
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = MaterialTheme.hack.gridLine)
    }
}
