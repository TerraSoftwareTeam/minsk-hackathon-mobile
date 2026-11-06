plugins {
    id("android.dev.compose")
}

android {
    namespace = "com.dev.lbsuhack.core.ui"
}

dependencies {
    api(project(":core:designsystem"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.icons.extended)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
