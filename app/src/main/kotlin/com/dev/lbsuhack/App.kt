package com.dev.lbsuhack

import android.app.Application
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.data.di.dataModule
import com.dev.lbsuhack.core.designsystem.di.designSystemModule
import com.dev.lbsuhack.core.localization.di.localizationModule
import com.dev.lbsuhack.core.network.di.networkModule
import com.dev.lbsuhack.feature.activities.di.activitiesModule
import com.dev.lbsuhack.feature.auth.di.authModule
import com.dev.lbsuhack.feature.events.di.eventsModule
import com.dev.lbsuhack.feature.feed.di.feedModule
import com.dev.lbsuhack.feature.profile.di.profileModule
import com.dev.lbsuhack.feature.requests.di.requestsModule
import com.dev.lbsuhack.layers.di.layersModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    singleOf(::DefaultStoreFactory) bind StoreFactory::class
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                appModule,
                designSystemModule,
                dataModule,
                localizationModule,
                layersModule,
                networkModule,
                activitiesModule,
                authModule,
                eventsModule,
                feedModule,
                profileModule,
                requestsModule,
            )
        }
    }
}
