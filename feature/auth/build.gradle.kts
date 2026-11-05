plugins {
    id("android.dev.feature")
    id("tech.dev.ktor")
}

android {
    namespace = "com.dev.lbsuhack.feature.auth"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:localization"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))

    implementation(libs.coroutines.core)
}
