import java.util.Properties

plugins {
    id("android.dev.application")
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val releaseStore = (
    providers.gradleProperty("RELEASE_STORE_FILE").orNull
        ?: providers.environmentVariable("RELEASE_STORE_FILE").orNull
        ?: keystoreProperties.getProperty("storeFile")
    )?.let { rootProject.file(it) }

val releaseStorePassword = providers.gradleProperty("RELEASE_STORE_PASSWORD").orNull
    ?: providers.environmentVariable("RELEASE_STORE_PASSWORD").orNull
    ?: keystoreProperties.getProperty("storePassword")

val releaseKeyAlias = providers.gradleProperty("RELEASE_KEY_ALIAS").orNull
    ?: providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
    ?: keystoreProperties.getProperty("keyAlias")

val releaseKeyPassword = providers.gradleProperty("RELEASE_KEY_PASSWORD").orNull
    ?: providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull
    ?: keystoreProperties.getProperty("keyPassword")

val hasReleaseSigning =
    releaseStore?.exists() == true &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()

val appVersionCode = providers.gradleProperty("VERSION_CODE").orNull
    ?.toIntOrNull()
    ?: 1

val appVersionName = providers.gradleProperty("VERSION_NAME").orNull
    ?.takeIf { it.isNotBlank() }
    ?: "1.0.0"

if (!hasReleaseSigning) {
    logger.warn(
        "Release signing is not configured. " +
            "`assembleRelease` will produce an unsigned APK. " +
            "Use keystore.properties locally or RELEASE_* secrets in CI."
    )
}

android {
    namespace = "com.dev.lbsuhack"

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseStore
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.dev.lbsuhack"
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)
    implementation(libs.androidx.appcompat)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Navigation 3
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // Core & Feature Modules
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:localization"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":layers"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:events"))
    implementation(project(":feature:feed"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:tournaments"))

    // MVIKotlin
    implementation(libs.bundles.mvikotlin)

    testImplementation(libs.bundles.unit.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
