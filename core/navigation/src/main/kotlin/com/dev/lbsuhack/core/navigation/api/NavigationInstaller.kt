package com.dev.lbsuhack.core.navigation.api

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

interface NavigationInstaller {
    fun install(scope: EntryProviderScope<NavKey>)
}

interface MainTabInstaller : NavigationInstaller {

    val id: String
    val rootRoute: NavKey

    @get:StringRes
    val titleRes: Int

    val icon: ImageVector
    val order: Int
}


interface SurveyStepInstaller : NavigationInstaller {
    val stepId: String
    val order: Int
}
