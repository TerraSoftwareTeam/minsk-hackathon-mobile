package com.dev.lbsuhack.feature.auth.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.auth.data.AuthRepositoryImpl
import com.dev.lbsuhack.feature.auth.data.parser.SchoolsByProfileParser
import com.dev.lbsuhack.feature.auth.data.remote.SchoolsByRemoteDataSource
import com.dev.lbsuhack.feature.auth.data.remote.SchoolsByRemoteDataSourceImpl
import com.dev.lbsuhack.feature.auth.data.repository.SchoolsByAuthRepositoryImpl
import com.dev.lbsuhack.feature.auth.domain.AuthRepository
import com.dev.lbsuhack.feature.auth.domain.repository.SchoolsByAuthRepository
import com.dev.lbsuhack.feature.auth.navigation.AuthNavigationInstaller
import com.dev.lbsuhack.feature.auth.presentation.login.LoginViewModel
import com.dev.lbsuhack.feature.auth.store.login.LoginStoreFactory
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    single<SchoolsByProfileParser> { SchoolsByProfileParser() }
    single<SchoolsByRemoteDataSource> { SchoolsByRemoteDataSourceImpl() }
    single<SchoolsByAuthRepository> {
        SchoolsByAuthRepositoryImpl(
            remoteDataSource = get(),
            parser = get(),
        )
    }
    single<AuthRepository> {
        AuthRepositoryImpl(
            schoolsByRepository = get(),
            httpClient = get<HttpClient>(named("unauthenticatedHttpClient")),
            tokenStorage = get(),
            userRepository = get(),
        )
    }

    single {
        LoginStoreFactory(
            storeFactory = get(),
            authRepository = get(),
            saveSession = get(),
        )
    }

    viewModelOf(::LoginViewModel)

    single { AuthNavigationInstaller() } bind NavigationInstaller::class
}
