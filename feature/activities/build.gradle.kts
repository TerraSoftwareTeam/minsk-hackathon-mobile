plugins {
    id("android.dev.feature")
}

android {
    namespace = "com.dev.lbsuhack.feature.activities"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:localization"))
    implementation(project(":core:navigation"))

    implementation(libs.coroutines.core)
    implementation(libs.zxing.core)
    implementation(libs.google.code.scanner)
}
