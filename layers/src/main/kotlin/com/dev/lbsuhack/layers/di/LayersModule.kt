package com.dev.lbsuhack.layers.di

import com.dev.lbsuhack.core.navigation.event.AppEventBus
import com.dev.lbsuhack.core.navigation.event.DefaultAppEventBus
import com.dev.lbsuhack.core.navigation.layer.NavigationLayer
import com.dev.lbsuhack.layers.auth.AuthLayer
import com.dev.lbsuhack.layers.global.GlobalLayer
import org.koin.dsl.bind
import org.koin.dsl.module

val layersModule = module {
    single<AppEventBus> { DefaultAppEventBus() }

    single { AuthLayer() } bind NavigationLayer::class
    single { GlobalLayer() } bind NavigationLayer::class
}
