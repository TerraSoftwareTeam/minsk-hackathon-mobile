plugins {
    id("android.dev.application")
}

android {
    namespace = "com.dev.lbsuhack"

    defaultConfig {
        applicationId = "com.dev.lbsuhack"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Navigation 3
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // Core & Feature Modules
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":layers"))
    implementation(project(":feature:home"))
    implementation(project(":feature:profile"))

    // MVIKotlin
    implementation(libs.bundles.mvikotlin)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
