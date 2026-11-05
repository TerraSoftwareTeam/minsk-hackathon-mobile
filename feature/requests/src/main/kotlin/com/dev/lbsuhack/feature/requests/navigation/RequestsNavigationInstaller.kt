package com.dev.lbsuhack.feature.requests.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.requests.ui.requests.RequestsScreen

class RequestsNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<RequestsRoute>(metadata = GlobalLayerMeta.entry()) { route ->
                val navigator = LocalNavigator.current

                RequestsScreen(
                    role = route.role,
                    onBack = navigator::back,
                )
            }
        }
    }
}
