package com.dev.lbsuhack.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.Navigator

class AppNavigator(
    private val backStack: NavBackStack<NavKey>,
) : Navigator {

    override val canGoBack: Boolean
        get() = backStack.size > 1

    override fun navigate(route: NavKey) {
        val existingIndex = backStack.indexOf(route)
        if (existingIndex >= 0) {
            while (backStack.lastIndex > existingIndex) backStack.removeAt(backStack.lastIndex)
            return
        }
        backStack.add(route)
    }

    override fun replaceAll(route: NavKey) {
        backStack.clear()
        backStack.add(route)
    }

    override fun back() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}
