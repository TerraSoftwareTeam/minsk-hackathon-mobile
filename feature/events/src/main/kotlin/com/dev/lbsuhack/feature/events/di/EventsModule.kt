package com.dev.lbsuhack.feature.events.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.events.data.EventRepositoryImpl
import com.dev.lbsuhack.feature.events.domain.EventRepository
import com.dev.lbsuhack.feature.events.navigation.EventsNavigationInstaller
import com.dev.lbsuhack.feature.events.presentation.details.EventDetailsViewModel
import com.dev.lbsuhack.feature.events.presentation.events.EventsViewModel
import com.dev.lbsuhack.feature.events.store.details.EventDetailsStoreFactory
import com.dev.lbsuhack.feature.events.store.events.EventsStoreFactory
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val eventsModule = module {
    single<EventRepository> { EventRepositoryImpl() }

    single { EventsStoreFactory(storeFactory = get(), eventRepository = get()) }
    single { EventDetailsStoreFactory(storeFactory = get(), eventRepository = get()) }

    viewModelOf(::EventsViewModel)
    viewModelOf(::EventDetailsViewModel)

    single { EventsNavigationInstaller() } bind NavigationInstaller::class
}
