package com.dev.lbsuhack.feature.profile.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.profile.data.ProfileRepositoryImpl
import com.dev.lbsuhack.feature.profile.data.UserProfileApi
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import com.dev.lbsuhack.feature.profile.navigation.ProfileNavigationInstaller
import com.dev.lbsuhack.feature.profile.presentation.profile.ProfileViewModel
import com.dev.lbsuhack.feature.profile.presentation.settings.SettingsViewModel
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStoreFactory
import com.dev.lbsuhack.feature.profile.store.settings.SettingsStoreFactory
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val profileModule = module {
    single {
        UserProfileApi(
            httpClient = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }

    single<ProfileRepository> {
        ProfileRepositoryImpl(
            userProfileApi = get(),
        )
    }

    singleOf(::ProfileStoreFactory)
    single { SettingsStoreFactory(storeFactory = get(), themeController = get(), languageController = get()) }

    viewModelOf(::ProfileViewModel)
    viewModelOf(::SettingsViewModel)

    single { ProfileNavigationInstaller() } bind NavigationInstaller::class
}
