package android.dev

import com.android.build.api.dsl.LibraryExtension
import helpers.catalog
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.getByType

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

configureKotlinAndroid(extensions.getByType<LibraryExtension>())

dependencies {
    add("implementation", catalog.findLibrary("koin-android").get())
}
