package helpers

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

fun Project.configureAndroidCompose(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.buildFeatures.compose = true

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        includeSourceInformation.set(true)
    }
}
