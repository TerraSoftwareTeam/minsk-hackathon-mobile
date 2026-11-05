package com.dev.lbsuhack.feature.tournaments.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.tournaments.data.TournamentRepositoryImpl
import com.dev.lbsuhack.feature.tournaments.domain.TournamentRepository
import com.dev.lbsuhack.feature.tournaments.navigation.TournamentsNavigationInstaller
import com.dev.lbsuhack.feature.tournaments.presentation.details.TournamentDetailsViewModel
import com.dev.lbsuhack.feature.tournaments.presentation.tournaments.TournamentsViewModel
import com.dev.lbsuhack.feature.tournaments.store.details.TournamentDetailsStoreFactory
import com.dev.lbsuhack.feature.tournaments.store.tournaments.TournamentsStoreFactory
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val tournamentsModule = module {
    single<TournamentRepository> { TournamentRepositoryImpl() }

    single { TournamentsStoreFactory(storeFactory = get(), tournamentRepository = get()) }
    single { TournamentDetailsStoreFactory(storeFactory = get(), tournamentRepository = get()) }

    viewModelOf(::TournamentsViewModel)
    viewModelOf(::TournamentDetailsViewModel)

    single { TournamentsNavigationInstaller() } bind NavigationInstaller::class
}
