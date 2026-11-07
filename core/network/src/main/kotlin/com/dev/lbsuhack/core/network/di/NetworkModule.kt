package com.dev.lbsuhack.core.network.di

import com.dev.lbsuhack.core.network.auth.AtomicFileTokenPersistence
import com.dev.lbsuhack.core.network.auth.EncryptedTokenStorage
import com.dev.lbsuhack.core.domain.session.ClearSessionUseCase
import com.dev.lbsuhack.core.network.auth.KeystoreTokenCipher
import com.dev.lbsuhack.core.network.auth.TokenStorage
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single {
        EncryptedTokenStorage(
            persistence = AtomicFileTokenPersistence(androidContext()),
            cipher = KeystoreTokenCipher(),
            ioDispatcher = Dispatchers.IO,
        )
    } bind TokenStorage::class

    single(named("unauthenticatedHttpClient")) {
        AppHttpClientFactory.createUnauthenticated(localeProvider = get())
    }
    single(named("authenticatedHttpClient")) {
        AppHttpClientFactory.createAuthenticated(
            localeProvider = get(),
            tokenStorage = get(),
        ) {
            get<ClearSessionUseCase>()()
            false
        }
    }
    single<HttpClient> { get(named("authenticatedHttpClient")) }
}
