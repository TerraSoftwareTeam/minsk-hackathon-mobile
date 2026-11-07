package com.dev.lbsuhack.feature.activities.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalAppNavigation
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.activities.data.ActivityPhotoSource
import com.dev.lbsuhack.feature.activities.ui.activities.ActivitiesScreen
import com.dev.lbsuhack.feature.activities.ui.component.LocalActivityPhotoLoader
import com.dev.lbsuhack.feature.activities.ui.create.CreateActivityScreen
import com.dev.lbsuhack.feature.activities.ui.details.ActivityDetailsScreen

class ActivitiesNavigationInstaller(
    private val photoSource: ActivityPhotoSource,
) : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<ActivitiesRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current
                val appNavigation = LocalAppNavigation.current

                CompositionLocalProvider(LocalActivityPhotoLoader provides photoSource::load) {
                    ActivitiesScreen(
                        onOpenDetails = { id ->
                            navigator.navigate(ActivityDetailsRoute(activityId = id))
                        },
                        onOpenCreate = {
                            navigator.navigate(CreateActivityRoute)
                        },
                        onOpenRequests = { role ->
                            appNavigation.openRequests(role)
                        },
                        onBack = if (navigator.canGoBack) navigator::back else null,
                        onOpenProfile = {
                            appNavigation.openProfile()
                        },
                    )
                }
            }

            entry<CreateActivityRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current

                CompositionLocalProvider(LocalActivityPhotoLoader provides photoSource::load) {
                    CreateActivityScreen(
                        onBack = navigator::back,
                        onCreated = { id ->
                            navigator.back()
                            navigator.navigate(ActivityDetailsRoute(activityId = id))
                        },
                    )
                }
            }

            entry<ActivityDetailsRoute>(metadata = GlobalLayerMeta.entry()) { route ->
                val navigator = LocalNavigator.current

                CompositionLocalProvider(LocalActivityPhotoLoader provides photoSource::load) {
                    ActivityDetailsScreen(
                        activityId = route.activityId,
                        onBack = navigator::back,
                    )
                }
            }
        }
    }
}
