package com.dev.lbsuhack.feature.home.di

import com.dev.lbsuhack.core.navigation.api.MainTabInstaller
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.home.navigation.HomeNavigationInstaller
import com.dev.lbsuhack.feature.home.presentation.HomeViewModel
import com.dev.lbsuhack.feature.home.store.HomeStoreFactory
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.binds
import org.koin.dsl.module



val homeModule = module {
    single { HomeStoreFactory(get()).create() }
    viewModelOf(::HomeViewModel)
    single { HomeNavigationInstaller() }
        .binds(arrayOf(MainTabInstaller::class, NavigationInstaller::class))
}
