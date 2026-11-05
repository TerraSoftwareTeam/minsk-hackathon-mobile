package com.dev.lbsuhack.core.data.di

import com.dev.lbsuhack.core.data.session.DefaultSessionRepository
import com.dev.lbsuhack.core.data.user.DefaultUserRepository
import com.dev.lbsuhack.core.domain.session.ClearSessionUseCase
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.RestoreSessionUseCase
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionRepository
import com.dev.lbsuhack.core.domain.user.GetCurrentUserUseCase
import com.dev.lbsuhack.core.domain.user.ObserveCurrentUserUseCase
import com.dev.lbsuhack.core.domain.user.UpdateUserCacheUseCase
import com.dev.lbsuhack.core.domain.user.UserRepository
import com.dev.lbsuhack.core.network.auth.TokenStorage
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single {
        DefaultSessionRepository(
            context = androidContext(),
            tokenStorage = getOrNull<TokenStorage>(),
            ioDispatcher = Dispatchers.IO,
        )
    } bind SessionRepository::class

    single {
        DefaultUserRepository(
            context = androidContext(),
            httpClient = get(),
            sessionRepository = get(),
            ioDispatcher = Dispatchers.IO,
        )
    } bind UserRepository::class

    single { ObserveSessionUseCase(repository = get()) }
    single { RestoreSessionUseCase(repository = get()) }
    single { SaveSessionUseCase(repository = get()) }
    single { ClearSessionUseCase(repository = get()) }

    single { ObserveCurrentUserUseCase(repository = get()) }
    single { GetCurrentUserUseCase(repository = get()) }
    single { UpdateUserCacheUseCase(repository = get()) }
}
