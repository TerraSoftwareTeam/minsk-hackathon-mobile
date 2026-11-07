package com.dev.lbsuhack.core.data.di

import com.dev.lbsuhack.core.data.PrefsKeyValueStorage
import com.dev.lbsuhack.core.data.push.DefaultNotificationSettingsRepository
import com.dev.lbsuhack.core.data.push.DefaultPushTokenRepository
import com.dev.lbsuhack.core.data.session.DefaultSessionRepository
import com.dev.lbsuhack.core.domain.push.NotificationSettingsRepository
import com.dev.lbsuhack.core.domain.push.ObserveNotificationsEnabledUseCase
import com.dev.lbsuhack.core.domain.push.PushTokenRepository
import com.dev.lbsuhack.core.domain.push.RegisterPushTokenUseCase
import com.dev.lbsuhack.core.domain.push.SetNotificationsEnabledUseCase
import com.dev.lbsuhack.core.domain.push.UnregisterPushTokenUseCase
import com.dev.lbsuhack.core.domain.session.SessionClearHook
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
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single {
        DefaultSessionRepository(
            storage = PrefsKeyValueStorage(androidContext(), "user_session"),
            tokenStorage = get<TokenStorage>(),
            ioDispatcher = Dispatchers.IO,
            clearHooks = { getAll<SessionClearHook>() },
        )
    } bind SessionRepository::class

    single {
        DefaultPushTokenRepository(
            storage = PrefsKeyValueStorage(androidContext(), "push_device"),
            httpClient = get(named("unauthenticatedHttpClient")),
            tokenStorage = get(),
            ioDispatcher = Dispatchers.IO,
        )
    } bind PushTokenRepository::class

    single { RegisterPushTokenUseCase(repository = get()) }
    single { UnregisterPushTokenUseCase(repository = get()) }

    single<NotificationSettingsRepository> {
        DefaultNotificationSettingsRepository(
            storage = PrefsKeyValueStorage(androidContext(), "push_device"),
            pushTokenProvider = get(),
            pushTokenRepository = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }

    single { ObserveNotificationsEnabledUseCase(repository = get()) }
    single { SetNotificationsEnabledUseCase(repository = get()) }

    single {
        DefaultUserRepository(
            storage = PrefsKeyValueStorage(androidContext(), "cached_user_profile"),
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
