package com.dev.lbsuhack.feature.tournaments.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.layer.GlobalLayerMeta
import com.dev.lbsuhack.feature.tournaments.ui.details.TournamentDetailsScreen
import com.dev.lbsuhack.feature.tournaments.ui.tournaments.TournamentsScreen

class TournamentsNavigationInstaller : NavigationInstaller {

    override fun install(scope: EntryProviderScope<NavKey>) {
        with(scope) {
            entry<TournamentsRoute>(metadata = GlobalLayerMeta.entry()) {
                val navigator = LocalNavigator.current

                TournamentsScreen(
                    onOpenDetails = { tournamentId ->
                        navigator.navigate(TournamentDetailsRoute(tournamentId = tournamentId))
                    },
                )
            }

            entry<TournamentDetailsRoute>(metadata = GlobalLayerMeta.entry()) { route ->
                val navigator = LocalNavigator.current

                TournamentDetailsScreen(
                    tournamentId = route.tournamentId,
                    onBack = navigator::back,
                )
            }
        }
    }
}
