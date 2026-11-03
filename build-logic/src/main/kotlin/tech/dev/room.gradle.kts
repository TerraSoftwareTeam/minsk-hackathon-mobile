package tech.dev

import com.google.devtools.ksp.gradle.KspExtension
import helpers.catalog
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    "implementation"(catalog.findLibrary("room-runtime").get())
    "implementation"(catalog.findLibrary("room-ktx").get())
    "ksp"(catalog.findLibrary("room-compiler").get())
}

extensions.configure<KspExtension> {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}
