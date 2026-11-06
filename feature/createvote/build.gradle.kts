plugins {
    id("android.dev.feature")
}

android {
    namespace = "com.dev.lbsuhack.feature.createvote"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:localization"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))

    implementation(libs.coroutines.core)
}
