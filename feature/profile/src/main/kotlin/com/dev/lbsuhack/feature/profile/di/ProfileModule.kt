package com.dev.lbsuhack.feature.profile.di

import com.dev.lbsuhack.core.navigation.api.MainTabInstaller
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.profile.navigation.ProfileNavigationInstaller
import com.dev.lbsuhack.feature.profile.presentation.ProfileViewModel
import com.dev.lbsuhack.feature.profile.store.ProfileStoreFactory
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.binds
import org.koin.dsl.module

val profileModule = module {
    single { ProfileStoreFactory(get()).create() }
    viewModelOf(::ProfileViewModel)
    single { ProfileNavigationInstaller() }
        .binds(arrayOf(MainTabInstaller::class, NavigationInstaller::class))
}
