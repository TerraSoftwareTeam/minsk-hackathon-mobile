package com.dev.lbsuhack.feature.activities.di

import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.feature.activities.data.ActivityRepositoryImpl
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.navigation.ActivitiesNavigationInstaller
import com.dev.lbsuhack.feature.activities.presentation.activities.ActivitiesViewModel
import com.dev.lbsuhack.feature.activities.presentation.create.CreateActivityViewModel
import com.dev.lbsuhack.feature.activities.presentation.details.ActivityDetailsViewModel
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStoreFactory
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStoreFactory
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStoreFactory
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val activitiesModule = module {
    singleOf(::ActivityRepositoryImpl) bind ActivityRepository::class

    singleOf(::ActivitiesStoreFactory)
    singleOf(::CreateActivityStoreFactory)
    singleOf(::ActivityDetailsStoreFactory)

    viewModelOf(::ActivitiesViewModel)
    viewModelOf(::CreateActivityViewModel)
    viewModelOf(::ActivityDetailsViewModel)

    singleOf(::ActivitiesNavigationInstaller) bind NavigationInstaller::class
}
