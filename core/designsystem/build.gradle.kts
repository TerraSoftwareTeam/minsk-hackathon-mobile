plugins {
    id("android.dev.compose")
}

android {
    namespace = "com.dev.lbsuhack.core.designsystem"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.bundles.unit.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
