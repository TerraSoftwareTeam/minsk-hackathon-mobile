package com.dev.lbsuhack.feature.activities.ui.activities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab

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

@Composable
private fun <T> ActivitiesFilterMenu(
    label: String,
    selection: T,
    options: List<T>,
    optionText: @Composable (T) -> String,
    isActive: Boolean,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(
                1.dp,
                if (isActive) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.outlineVariant,
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = optionText(selection),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionText(option)) },
                    trailingIcon = {
                        if (option == selection) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun ActivityTab.labelRes(): Int = when (this) {
    ActivityTab.ALL -> R.string.activities_tab_all
    ActivityTab.ON_VOTE -> R.string.activities_tab_recruiting
    ActivityTab.ON_ADMIN_MODERATION -> R.string.activities_tab_pending_admin
    ActivityTab.FINALIZED -> R.string.activities_tab_approved
    ActivityTab.MY_PROPOSALS -> R.string.activities_tab_my_proposals
    ActivityTab.MY_VOTES -> R.string.activities_tab_my_participations
}
