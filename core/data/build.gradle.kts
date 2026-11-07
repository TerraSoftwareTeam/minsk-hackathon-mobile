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

    testImplementation(libs.bundles.unit.test)
    testImplementation("io.ktor:ktor-client-mock:3.0.3")
}
