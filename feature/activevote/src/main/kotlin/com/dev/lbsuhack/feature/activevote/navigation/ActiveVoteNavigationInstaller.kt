package com.dev.lbsuhack.feature.activevote.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.activevote.ui.activevote.ActiveVoteScreen

class ActiveVoteNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<ActiveVoteRoute>(metadata = GlobalLayerMeta.entry()) { route ->
                val navigator = LocalNavigator.current

                ActiveVoteScreen(
                    voteId = route.voteId,
                    onBack = navigator::back,
                )
            }
        }
    }
}
