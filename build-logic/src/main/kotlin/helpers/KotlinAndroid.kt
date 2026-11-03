package helpers

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.catalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    val compileSdk = catalog.findVersion("compileSdk").get().requiredVersion.toInt()
    val minSdk = catalog.findVersion("minSdk").get().requiredVersion.toInt()

    commonExtension.compileSdk = compileSdk
    commonExtension.defaultConfig.minSdk = minSdk
    commonExtension.compileOptions.sourceCompatibility = JavaVersion.VERSION_21
    commonExtension.compileOptions.targetCompatibility = JavaVersion.VERSION_21
    commonExtension.testOptions.unitTests.isReturnDefaultValues = true
}
