package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackFilterChip
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import com.dev.lbsuhack.feature.activities.store.activities.QuickFilterTabs
import com.dev.lbsuhack.feature.activities.ui.activities.mapper.labelRes

@Composable
internal fun ActivitiesFilters(
    selectedCategory: ActivityCategory?,
    selectedTab: ActivityTab,
    showQuickFilters: Boolean,
    onSelectCategory: (ActivityCategory?) -> Unit,
    onSelectTab: (ActivityTab) -> Unit,
) {
    var isTopicSheetOpen by rememberSaveable { mutableStateOf(false) }
    var lastCategory by remember { mutableStateOf(selectedCategory) }
    if (selectedCategory != null) lastCategory = selectedCategory

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showQuickFilters) {
                QuickFilterTabs.forEach { tab ->
                    HackFilterChip(
                        text = stringResource(tab.labelRes()),
                        selected = selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        ActivitiesTopicButton(
            isActive = selectedCategory != null,
            onClick = { isTopicSheetOpen = true },
        )
    }

    AnimatedVisibility(
        visible = selectedCategory != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        val category = selectedCategory ?: lastCategory
        if (category != null) {
            HackFilterChip(
                text = stringResource(category.labelRes()),
                selected = true,
                onClick = { onSelectCategory(null) },
                trailingIcon = Icons.Rounded.Close,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            )
        }
    }

    if (isTopicSheetOpen) {
        ActivitiesTopicSheet(
            selected = selectedCategory,
            onSelect = { category ->
                onSelectCategory(category)
                isTopicSheetOpen = false
            },
            onDismiss = { isTopicSheetOpen = false },
        )
    }
}
