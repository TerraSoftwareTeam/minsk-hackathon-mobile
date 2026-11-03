package android.dev

import com.android.build.api.dsl.LibraryExtension
import helpers.configureAndroidCompose
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.getByType

plugins {
    id("android.dev.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

configureKotlinAndroid(extensions.getByType<LibraryExtension>())
configureAndroidCompose(extensions.getByType<LibraryExtension>())