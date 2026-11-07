package com.dev.lbsuhack.core.navigation.api

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.domain.model.UserRole

interface Navigator {
    val canGoBack: Boolean
    fun navigate(route: NavKey)
    fun replaceAll(route: NavKey)
    fun back()
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No Navigator provided in CompositionLocal")
}

interface AppNavigation {
    fun openProfile()
    fun openRequests(role: UserRole)
    fun openCreateActivity()
    fun openActivities()
    fun openActivity(activityId: String)
}

val LocalAppNavigation = staticCompositionLocalOf<AppNavigation> {
    error("No AppNavigation provided in CompositionLocal")
}
