package com.dev.lbsuhack.core.network.di

import com.dev.lbsuhack.core.network.auth.EncryptedTokenStorage
import com.dev.lbsuhack.core.network.auth.TokenRefresher
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
            context = androidContext(),
            ioDispatcher = Dispatchers.IO,
        )
    } bind TokenStorage::class

    single(named("unauthenticatedHttpClient")) {
        AppHttpClientFactory.createUnauthenticated()
    }
    single {
        TokenRefresher(
            tokenStorage = get(),
            unauthenticatedClientProvider = {
                get<HttpClient>(named("unauthenticatedHttpClient"))
            },
        )
    }

    single(named("authenticatedHttpClient")) {
        AppHttpClientFactory.createAuthenticated(tokenStorage = get()) {
            get<TokenRefresher>().refresh()
        }
    }
    single<HttpClient> { get(named("authenticatedHttpClient")) }
}
