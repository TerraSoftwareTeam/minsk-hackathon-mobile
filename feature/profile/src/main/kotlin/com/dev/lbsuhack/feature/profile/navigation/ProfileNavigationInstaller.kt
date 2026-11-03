package com.dev.lbsuhack.feature.profile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.layer.MainLayerMeta
import com.dev.lbsuhack.core.navigation.api.MainTabInstaller
import com.dev.lbsuhack.core.navigation.routes.ProfileRoute
import com.dev.lbsuhack.feature.profile.ui.ProfileScreen

class ProfileNavigationInstaller : MainTabInstaller {
    override val id: String = "profile"
    override val rootRoute: NavKey = ProfileRoute
    override val title: String = "Профиль"
    override val icon: ImageVector = Icons.Default.Person
    override val order: Int = 1

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<ProfileRoute>(metadata = MainLayerMeta.tab("profile")) {
                ProfileScreen()
            }
        }
    }
}