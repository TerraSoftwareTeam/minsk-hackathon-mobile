package com.dev.lbsuhack.feature.feed.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalAppNavigation
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.feed.ui.details.FeedDetailsScreen
import com.dev.lbsuhack.feature.feed.ui.feed.FeedScreen

class FeedNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<FeedRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current
                val appNavigation = LocalAppNavigation.current

                FeedScreen(
                    onOpenDetails = { eventId ->
                        navigator.navigate(FeedDetailsRoute(eventId = eventId))
                    },
                    onOpenProfile = appNavigation::openProfile,
                )
            }

            entry<FeedDetailsRoute>(metadata = GlobalLayerMeta.entry()) { route ->
                val navigator = LocalNavigator.current

                FeedDetailsScreen(
                    eventId = route.eventId,
                    onBack = navigator::back,
                )
            }
        }
    }
}
