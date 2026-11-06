package com.dev.lbsuhack.feature.createvote.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.createvote.ui.createvote.CreateVoteScreen

class CreateVoteNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<CreateVoteRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current

                CreateVoteScreen(
                    onBack = navigator::back,
                    onCreated = navigator::back,
                )
            }
        }
    }
}
