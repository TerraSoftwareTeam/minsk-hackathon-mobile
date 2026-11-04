package com.dev.lbsuhack.navigation

import android.util.Log
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.Navigator

class AppNavigator(
    private val backStack: NavBackStack<NavKey>,
    private val startRoute: NavKey,
) : Navigator {

    override fun navigate(route: NavKey) {
        if (backStack.lastOrNull() == route) return
        backStack.add(route)
        Log.d("Nav", "stack=${backStack.toList()}")
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

    override fun switchTab(root: NavKey) {
        while (backStack.size > 1) backStack.removeLastOrNull()
        if (root != startRoute) backStack.add(root)
    }
}
