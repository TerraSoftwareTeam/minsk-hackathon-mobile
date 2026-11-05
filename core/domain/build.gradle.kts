plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    // The session contract exposes StateFlow, so coroutines are part of the API.
    api(libs.coroutines.core)
}
