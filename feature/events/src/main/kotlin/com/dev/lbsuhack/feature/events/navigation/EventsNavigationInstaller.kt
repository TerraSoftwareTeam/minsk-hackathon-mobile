package com.dev.lbsuhack.feature.events.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.events.ui.details.EventDetailsScreen
import com.dev.lbsuhack.feature.events.ui.events.EventsScreen

class EventsNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<EventsRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current

                EventsScreen(
                    onOpenDetails = { eventId ->
                        navigator.navigate(EventDetailsRoute(eventId = eventId))
                    },
                )
            }

            entry<EventDetailsRoute>(metadata = GlobalLayerMeta.entry()) { route ->
                val navigator = LocalNavigator.current

                EventDetailsScreen(
                    eventId = route.eventId,
                    onBack = navigator::back,
                )
            }
        }
    }
}
