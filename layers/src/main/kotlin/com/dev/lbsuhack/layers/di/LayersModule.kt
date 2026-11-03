package com.dev.lbsuhack.layers.di

import com.dev.lbsuhack.core.navigation.event.AppEventBus
import com.dev.lbsuhack.core.navigation.event.DefaultAppEventBus
import com.dev.lbsuhack.core.navigation.api.MainTabInstaller
import com.dev.lbsuhack.core.navigation.layer.NavigationLayer
import com.dev.lbsuhack.layers.main.MainLayer
import org.koin.dsl.bind
import org.koin.dsl.module


val layersModule = module {
    single<AppEventBus> { DefaultAppEventBus() }

    single {
        MainLayer(tabs = getAll<MainTabInstaller>())
    } bind NavigationLayer::class
}
