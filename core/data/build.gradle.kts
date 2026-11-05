plugins {
    id("android.dev.library")
    id("tech.dev.ktor")
}

android {
    namespace = "com.dev.lbsuhack.core.data"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:network"))

    implementation(libs.coroutines.core)
    implementation(libs.koin.android)
}
