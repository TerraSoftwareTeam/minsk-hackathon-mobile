package com.dev.lbsuhack.core.data.di

import com.dev.lbsuhack.core.data.session.DefaultSessionRepository
import com.dev.lbsuhack.core.domain.session.ClearSessionUseCase
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.RestoreSessionUseCase
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single {
        DefaultSessionRepository(
            context = androidContext(),
            ioDispatcher = Dispatchers.IO,
        )
    } bind SessionRepository::class

    single { ObserveSessionUseCase(repository = get()) }
    single { RestoreSessionUseCase(repository = get()) }
    single { SaveSessionUseCase(repository = get()) }
    single { ClearSessionUseCase(repository = get()) }
}
