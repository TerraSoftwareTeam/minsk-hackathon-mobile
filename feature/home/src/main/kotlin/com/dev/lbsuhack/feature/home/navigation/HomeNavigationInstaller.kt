package com.dev.lbsuhack.feature.home.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.layer.MainLayerMeta
import com.dev.lbsuhack.core.navigation.api.MainTabInstaller
import com.dev.lbsuhack.core.navigation.routes.DetailsRoute
import com.dev.lbsuhack.core.navigation.routes.HomeRoute
import com.dev.lbsuhack.feature.home.R
import com.dev.lbsuhack.feature.home.ui.DetailsScreen
import com.dev.lbsuhack.feature.home.ui.HomeScreen

class HomeNavigationInstaller : MainTabInstaller {
    override val rootRoute: NavKey = HomeRoute
    override val titleRes: Int = R.string.home_tab_title
    override val icon: ImageVector = Icons.Default.Home
    override val order: Int = 0

    override val id: String = "home"

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<HomeRoute>(metadata = MainLayerMeta.tab("home")) {
                val navigator = LocalNavigator.current
                HomeScreen(
                    onNavigateToDetails = { id ->
                        navigator.navigate(DetailsRoute(itemId = id))
                    },
                )
            }
            entry<DetailsRoute> { route ->
                val navigator = LocalNavigator.current
                DetailsScreen(
                    itemId = route.itemId,
                    onBack = { navigator.back() },
                )
            }
        }
    }
}
