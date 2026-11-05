package com.dev.lbsuhack.core.navigation.api

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

interface NavigationInstaller {
    fun install(scope: EntryProviderScope<NavKey>)
}
