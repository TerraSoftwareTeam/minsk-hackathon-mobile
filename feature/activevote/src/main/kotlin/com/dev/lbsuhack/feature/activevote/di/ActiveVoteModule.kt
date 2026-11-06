package com.dev.lbsuhack.feature.activevote.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.activevote.data.HardcodedActiveVoteRepository
import com.dev.lbsuhack.feature.activevote.domain.ActiveVoteRepository
import com.dev.lbsuhack.feature.activevote.navigation.ActiveVoteNavigationInstaller
import com.dev.lbsuhack.feature.activevote.presentation.activevote.ActiveVoteViewModel
import com.dev.lbsuhack.feature.activevote.store.activevote.ActiveVoteStoreFactory
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val activeVoteModule = module {
    singleOf(::HardcodedActiveVoteRepository) bind ActiveVoteRepository::class

    singleOf(::ActiveVoteStoreFactory)

    viewModelOf(::ActiveVoteViewModel)

    singleOf(::ActiveVoteNavigationInstaller) bind NavigationInstaller::class
}
