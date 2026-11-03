package android.dev

import com.android.build.api.dsl.LibraryExtension
import helpers.catalog
import helpers.configureAndroidCompose
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.getByType

plugins {
    id("android.dev.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

configureKotlinAndroid(extensions.getByType<LibraryExtension>())
configureAndroidCompose(extensions.getByType<LibraryExtension>())

dependencies {
    add("implementation", catalog.findLibrary("koin-core").get())
    add("implementation", catalog.findLibrary("koin-compose").get())
    add("implementation", catalog.findBundle("mvikotlin").get())
    add("implementation", catalog.findLibrary("kotlinx-serialization-json").get())
    add("implementation", catalog.findBundle("navigation3").get())
    add("implementation", catalog.findLibrary("androidx-lifecycle-viewmodel-compose").get())
    add("implementation", catalog.findLibrary("androidx-compose-material3").get())
    add("implementation", catalog.findLibrary("androidx-compose-icons-extended").get())
    add("testImplementation", catalog.findBundle("unit-test").get())
}
