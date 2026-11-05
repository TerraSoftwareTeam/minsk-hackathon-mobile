plugins {
    id("android.dev.library")
}

android {
    namespace = "com.dev.lbsuhack.core.data"
}

dependencies {
    api(project(":core:domain"))

    implementation(libs.coroutines.core)
    implementation(libs.koin.android)
}
