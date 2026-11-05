package com.dev.lbsuhack.feature.feed.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.feed.data.FeedRepositoryImpl
import com.dev.lbsuhack.feature.feed.domain.FeedRepository
import com.dev.lbsuhack.feature.feed.navigation.FeedNavigationInstaller
import com.dev.lbsuhack.feature.feed.presentation.details.FeedDetailsViewModel
import com.dev.lbsuhack.feature.feed.presentation.feed.FeedViewModel
import com.dev.lbsuhack.feature.feed.store.details.FeedDetailsStoreFactory
import com.dev.lbsuhack.feature.feed.store.feed.FeedStoreFactory
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val feedModule = module {
    single<FeedRepository> { FeedRepositoryImpl() }

    single { FeedStoreFactory(storeFactory = get(), feedRepository = get()) }
    single { FeedDetailsStoreFactory(storeFactory = get(), feedRepository = get()) }

    viewModelOf(::FeedViewModel)
    viewModelOf(::FeedDetailsViewModel)

    single { FeedNavigationInstaller() } bind NavigationInstaller::class
}
