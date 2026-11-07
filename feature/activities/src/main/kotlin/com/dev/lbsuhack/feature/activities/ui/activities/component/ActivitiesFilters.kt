package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import com.dev.lbsuhack.feature.activities.ui.activities.mapper.labelRes

@Composable
internal fun ActivitiesFilters(
    selectedCategory: ActivityCategory?,
    selectedTab: ActivityTab,
    onSelectCategory: (ActivityCategory?) -> Unit,
    onSelectTab: (ActivityTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ActivitiesFilterMenu(
            label = stringResource(R.string.activities_filter_status_label),
            selection = selectedTab,
            options = ActivityTab.entries,
            optionText = { stringResource(it.labelRes()) },
            isActive = selectedTab != ActivityTab.ALL,
            onSelect = onSelectTab,
            modifier = Modifier.weight(1f),
        )
        ActivitiesFilterMenu(
            label = stringResource(R.string.activities_filter_category_label),
            selection = selectedCategory,
            options = listOf(null) + ActivityCategory.entries,
            optionText = { category ->
                stringResource(category?.labelRes() ?: R.string.activities_filter_all_categories)
            },
            isActive = selectedCategory != null,
            onSelect = onSelectCategory,
            modifier = Modifier.weight(1f),
        )
    }
}
