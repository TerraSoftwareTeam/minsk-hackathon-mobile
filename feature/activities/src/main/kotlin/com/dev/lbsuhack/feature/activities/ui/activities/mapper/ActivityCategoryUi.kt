package com.dev.lbsuhack.feature.activities.ui.activities.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.ui.graphics.vector.ImageVector
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory

internal fun ActivityCategory.icon(): ImageVector = when (this) {
    ActivityCategory.SPORTS -> Icons.Outlined.SportsBasketball
    ActivityCategory.INTELLECTUAL -> Icons.Outlined.Casino
    ActivityCategory.CULTURAL -> Icons.Outlined.Museum
    ActivityCategory.CREATIVE -> Icons.Outlined.Palette
    ActivityCategory.EDUCATIONAL -> Icons.Outlined.Computer
    ActivityCategory.ENVIRONMENTAL -> Icons.Outlined.CalendarMonth
    ActivityCategory.TOURISM -> Icons.Outlined.LocationOn
    ActivityCategory.ENTERTAINMENT -> Icons.Outlined.Movie
    ActivityCategory.OTHER -> Icons.Outlined.AutoAwesome
}

internal fun ActivityCategory.labelRes(): Int = when (this) {
    ActivityCategory.SPORTS -> R.string.activities_filter_sports
    ActivityCategory.INTELLECTUAL -> R.string.activities_filter_intellectual
    ActivityCategory.CULTURAL -> R.string.activities_filter_cultural
    ActivityCategory.CREATIVE -> R.string.activities_filter_creative
    ActivityCategory.EDUCATIONAL -> R.string.activities_filter_educational
    ActivityCategory.ENVIRONMENTAL -> R.string.activities_filter_environmental
    ActivityCategory.TOURISM -> R.string.activities_filter_tourism
    ActivityCategory.ENTERTAINMENT -> R.string.activities_filter_entertainment
    ActivityCategory.OTHER -> R.string.activities_filter_other
}
