package com.dev.lbsuhack.feature.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.AuthLayerMeta
import com.dev.lbsuhack.feature.auth.ui.login.LoginScreen
import com.dev.lbsuhack.feature.auth.ui.register.RegisterScreen

class AuthNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<AuthLoginRoute>(metadata = AuthLayerMeta.entry()) {
                val navigator = LocalNavigator.current
                LoginScreen(
                    onOpenRegister = { navigator.navigate(AuthRegisterRoute) },
                )
            }

            entry<AuthRegisterRoute>(metadata = AuthLayerMeta.entry()) {
                val navigator = LocalNavigator.current
                RegisterScreen(
                    onBack = { navigator.back() },
                )
            }
        }
    }
}
