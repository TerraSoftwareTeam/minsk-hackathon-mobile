package com.dev.lbsuhack.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {
    private data object Profile : NavKey
    private data object CreateEvent : NavKey
    private data class EventDetails(val id: String) : NavKey

    @Test
    fun replacingCreationOpensDetailsAndBackReturnsToOrigin() {
        val stack = NavBackStack<NavKey>().apply { addAll(listOf(Profile, CreateEvent)) }
        val navigator = AppNavigator(stack)
        val details = EventDetails("created")

        navigator.replaceCurrent(details)

        assertEquals(listOf(Profile, details), stack.toList())
        assertTrue(navigator.canGoBack)
        navigator.back()
        assertEquals(listOf(Profile), stack.toList())
    }

    @Test
    fun replacingCreationReusesAnExistingDetailsEntry() {
        val details = EventDetails("created")
        val stack = NavBackStack<NavKey>().apply { addAll(listOf(Profile, details, CreateEvent)) }

        AppNavigator(stack).replaceCurrent(details)

        assertEquals(listOf(Profile, details), stack.toList())
    }
}
