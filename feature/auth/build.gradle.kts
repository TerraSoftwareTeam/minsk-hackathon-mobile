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
    implementation(project(":core:ui"))

    testImplementation("io.ktor:ktor-client-mock:3.0.3")

    implementation(libs.coroutines.core)
}
