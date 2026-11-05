package com.dev.lbsuhack.feature.profile.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.profile.data.ProfileRepositoryImpl
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import com.dev.lbsuhack.feature.profile.navigation.ProfileNavigationInstaller
import com.dev.lbsuhack.feature.profile.presentation.profile.ProfileViewModel
import com.dev.lbsuhack.feature.profile.presentation.settings.SettingsViewModel
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStoreFactory
import com.dev.lbsuhack.feature.profile.store.settings.SettingsStoreFactory
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> { ProfileRepositoryImpl() }

    single {
        ProfileStoreFactory(
            storeFactory = get(),
            profileRepository = get(),
            observeSession = get(),
            clearSession = get(),
        )
    }
    single { SettingsStoreFactory(storeFactory = get(), themeController = get(), languageController = get()) }

    viewModelOf(::ProfileViewModel)
    viewModelOf(::SettingsViewModel)

    single { ProfileNavigationInstaller() } bind NavigationInstaller::class
}
