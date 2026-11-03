package com.dev.lbsuhack.core.navigation.api

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey


interface Navigator {
    fun navigate(route: NavKey)
    fun replaceAll(route: NavKey)
    fun back()

    fun switchTab(root: NavKey)
}


val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No Navigator provided in CompositionLocal")
}
