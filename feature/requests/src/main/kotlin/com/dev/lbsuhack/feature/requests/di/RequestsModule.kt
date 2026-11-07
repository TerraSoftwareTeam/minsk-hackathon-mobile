package com.dev.lbsuhack.feature.requests.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.requests.data.EventReviewRequestRepository
import com.dev.lbsuhack.feature.requests.domain.ReviewRequestRepository
import com.dev.lbsuhack.feature.requests.navigation.RequestsNavigationInstaller
import com.dev.lbsuhack.feature.requests.presentation.requests.RequestsViewModel
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStoreFactory
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val requestsModule = module {
    single<ReviewRequestRepository> {
        EventReviewRequestRepository(
            httpClient = get(),
            ioDispatcher = Dispatchers.IO,
            observeSession = get(),
        )
    }

    single {
        RequestsStoreFactory(
            storeFactory = get(),
            requestRepository = get(),
            observeSessionUseCase = get(),
        )
    }

    viewModelOf(::RequestsViewModel)

    singleOf(::RequestsNavigationInstaller) bind NavigationInstaller::class
}
