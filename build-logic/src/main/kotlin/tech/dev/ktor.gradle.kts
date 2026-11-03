package tech.dev

import helpers.addCatalogBundle
import helpers.catalog
import org.gradle.kotlin.dsl.dependencies

fun addKtorAndroidDependencies() {
    dependencies {
        addCatalogBundle(
            configurationName = "implementation",
            catalog = catalog,
            bundleName = "ktor-client"
        )
    }
}

pluginManager.withPlugin("com.android.application") {
    addKtorAndroidDependencies()
}

pluginManager.withPlugin("com.android.library") {
    addKtorAndroidDependencies()
}
