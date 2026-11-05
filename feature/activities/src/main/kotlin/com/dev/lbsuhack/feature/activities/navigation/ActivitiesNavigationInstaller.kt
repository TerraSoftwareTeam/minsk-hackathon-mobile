package com.dev.lbsuhack.feature.activities.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalAppNavigation
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.activities.ui.activities.ActivitiesScreen
import com.dev.lbsuhack.feature.activities.ui.create.CreateActivityScreen
import com.dev.lbsuhack.feature.activities.ui.details.ActivityDetailsScreen

class ActivitiesNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<ActivitiesRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current
                val appNavigation = LocalAppNavigation.current

                ActivitiesScreen(
                    onOpenDetails = { id ->
                        navigator.navigate(ActivityDetailsRoute(activityId = id))
                    },
                    onOpenCreate = {
                        navigator.navigate(CreateActivityRoute)
                    },
                    onOpenProfile = {
                        appNavigation.openProfile()
                    },
                )
            }

            entry<CreateActivityRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current

                CreateActivityScreen(
                    onBack = navigator::back,
                    onCreated = { id ->
                        navigator.navigate(ActivityDetailsRoute(activityId = id))
                    },
                )
            }

            entry<ActivityDetailsRoute>(metadata = GlobalLayerMeta.entry()) { route ->
                val navigator = LocalNavigator.current

                ActivityDetailsScreen(
                    activityId = route.activityId,
                    onBack = navigator::back,
                )
            }
        }
    }
}
