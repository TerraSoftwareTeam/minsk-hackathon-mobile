package android.dev

import com.android.build.api.dsl.ApplicationExtension
import helpers.catalog
import helpers.configureAndroidCompose
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

configureKotlinAndroid(extensions.getByType<ApplicationExtension>()) 
configureAndroidCompose(extensions.getByType<ApplicationExtension>())

configure<ApplicationExtension> {
    defaultConfig {
        targetSdk = catalog.findVersion("targetSdk").get().requiredVersion.toInt()
    }
}
