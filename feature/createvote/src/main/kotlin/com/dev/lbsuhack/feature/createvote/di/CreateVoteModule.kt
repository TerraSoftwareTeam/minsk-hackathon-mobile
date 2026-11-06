package com.dev.lbsuhack.feature.createvote.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.createvote.data.HardcodedVoteRepository
import com.dev.lbsuhack.feature.createvote.domain.VoteRepository
import com.dev.lbsuhack.feature.createvote.navigation.CreateVoteNavigationInstaller
import com.dev.lbsuhack.feature.createvote.presentation.createvote.CreateVoteViewModel
import com.dev.lbsuhack.feature.createvote.store.createvote.CreateVoteStoreFactory
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val createVoteModule = module {
    singleOf(::HardcodedVoteRepository) bind VoteRepository::class

    singleOf(::CreateVoteStoreFactory)

    viewModelOf(::CreateVoteViewModel)

    singleOf(::CreateVoteNavigationInstaller) bind NavigationInstaller::class
}
