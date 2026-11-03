package com.dev.lbsuhack.core.network.di

import com.dev.lbsuhack.core.network.auth.EncryptedTokenStorage
import com.dev.lbsuhack.core.network.auth.TokenRefresher
import com.dev.lbsuhack.core.network.auth.TokenStorage
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    singleOf(::EncryptedTokenStorage).bind<TokenStorage>()

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
