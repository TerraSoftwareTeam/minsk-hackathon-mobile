plugins {
    id("android.dev.feature")
}

android {
    namespace = "com.dev.lbsuhack.feature.profile"
}

dependencies {
    implementation(project(":core:navigation"))
}
