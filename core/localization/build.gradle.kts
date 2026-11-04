plugins {
    id("android.dev.compose")
}

android {
    namespace = "com.dev.lbsuhack.core.localization"
}

dependencies {
    api(project(":core:domain"))

    implementation(libs.coroutines.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
}
