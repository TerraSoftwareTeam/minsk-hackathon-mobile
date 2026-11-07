plugins {
    id("android.dev.feature")
    id("tech.dev.ktor")
}

android {
    namespace = "com.dev.lbsuhack.feature.activities"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:localization"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))

    implementation(libs.coroutines.core)
    implementation(libs.zxing.core)
    implementation(libs.google.code.scanner)

    testImplementation("io.ktor:ktor-client-mock:3.0.3")
}
