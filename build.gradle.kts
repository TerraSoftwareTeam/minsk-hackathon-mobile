// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.openapi.generator) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kover)
    id("tech.dev.generator")
}

// Kover merges coverage of every unit test into one report. The root project
// only orchestrates it: each module is instrumented individually by the
// `android.dev.*` convention plugins.
//
// When a new module is added, register it here too.
dependencies {
    kover(project(":app"))
    kover(project(":core:data"))
    kover(project(":core:designsystem"))
    kover(project(":core:domain"))
    kover(project(":core:localization"))
    kover(project(":core:navigation"))
    kover(project(":core:network"))
    kover(project(":core:ui"))
    kover(project(":feature:activities"))
    kover(project(":feature:auth"))
    kover(project(":feature:profile"))
    kover(project(":feature:requests"))
    kover(project(":layers"))
}

kover {
    reports {
        filters {
            excludes {
                // Generated and boilerplate code has no unit tests and only
                // dilutes the report.
                classes(
                    "*.BuildConfig",
                    "*.di.*",
                    "*ComposableSingletons*",
                    "*PreviewKt",
                    "*HackColorSchemeKt",
                    "*HackTypographyKt",
                    "*HackShapesKt",
                    "com.dev.lbsuhack.core.network.generated.*",
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}
