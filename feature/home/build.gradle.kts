plugins {
    id("android.dev.feature")
}

android {
    namespace = "com.dev.lbsuhack.feature.home"
}

dependencies {
    implementation(project(":core:navigation"))
    implementation(project(":core:localization"))
}
