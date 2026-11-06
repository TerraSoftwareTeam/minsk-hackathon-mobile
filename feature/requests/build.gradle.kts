plugins {
    id("android.dev.feature")
    id("tech.dev.ktor")
}

android {
    namespace = "com.dev.lbsuhack.feature.requests"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:localization"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))

    implementation(libs.coroutines.core)

    testImplementation("io.ktor:ktor-client-mock:3.0.3")
}
