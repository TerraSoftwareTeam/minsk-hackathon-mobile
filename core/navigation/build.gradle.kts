plugins {
    id("android.dev.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dev.lbsuhack.core.navigation"
}

dependencies {
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)
    implementation(platform(libs.androidx.compose.bom))
}
