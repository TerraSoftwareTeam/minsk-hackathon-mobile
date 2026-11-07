package com.dev.lbsuhack.feature.requests.ui.labels

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
import com.dev.lbsuhack.feature.requests.R

internal fun categoryLabelRes(categoryKey: String): Int = when (categoryKey) {
    "SPORTS" -> R.string.requests_category_sports
    "INTELLECTUAL" -> R.string.requests_category_intellectual
    "CULTURAL" -> R.string.requests_category_cultural
    "CREATIVE" -> R.string.requests_category_creative
    "EDUCATIONAL" -> R.string.requests_category_educational
    "ENVIRONMENTAL" -> R.string.requests_category_environmental
    "TOURISM" -> R.string.requests_category_tourism
    "ENTERTAINMENT" -> R.string.requests_category_entertainment
    else -> R.string.requests_category_other
}

internal fun categoryIcon(categoryKey: String): ImageVector = when (categoryKey) {
    "SPORTS" -> Icons.Outlined.SportsBasketball
    "INTELLECTUAL" -> Icons.Outlined.Casino
    "CULTURAL" -> Icons.Outlined.Museum
    "CREATIVE" -> Icons.Outlined.Palette
    "EDUCATIONAL" -> Icons.Outlined.Computer
    "ENVIRONMENTAL" -> Icons.Outlined.CalendarMonth
    "TOURISM" -> Icons.Outlined.LocationOn
    "ENTERTAINMENT" -> Icons.Outlined.Movie
    else -> Icons.Outlined.AutoAwesome
}
