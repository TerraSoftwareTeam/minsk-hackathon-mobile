package com.dev.lbsuhack.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.profile.ui.profile.ProfileScreen
import com.dev.lbsuhack.feature.profile.ui.settings.ProfileSettingsScreen

class ProfileNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<ProfileRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current

                ProfileScreen(
                    onBack = navigator::back,
                    onOpenSettings = { navigator.navigate(ProfileSettingsRoute) },
                )
            }

            entry<ProfileSettingsRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current

                ProfileSettingsScreen(
                    onBack = navigator::back,
                )
            }
        }
    }
}
