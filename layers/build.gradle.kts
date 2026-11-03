plugins {
    id("android.dev.compose")
}

android {
    namespace = "com.dev.lbsuhack.layers"
}

dependencies {
    implementation(project(":core:navigation"))

    implementation(libs.bundles.navigation3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}
