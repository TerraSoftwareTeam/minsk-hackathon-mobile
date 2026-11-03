plugins {
    id("android.dev.library")
    id("tech.dev.ktor")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.openapi.generator)
}

val generatedOpenApiDir = layout.buildDirectory.dir("generated/openapi")

android {
    namespace = "com.dev.lbsuhack.core.network"

    sourceSets.named("main") {
        java.srcDir(generatedOpenApiDir.map { it.dir("src/main/kotlin") })
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    library.set("jvm-retrofit2")
    inputSpec.set(rootProject.file("api/openapi.yaml").absolutePath)
    outputDir.set(generatedOpenApiDir.get().asFile.absolutePath)
    modelPackage.set("com.dev.lbsuhack.core.network.generated.model")
    globalProperties.set(
        mapOf(
            "models" to "",
            "modelDocs" to "false",
            "modelTests" to "false",
        )
    )
    configOptions.set(
        mapOf(
            "serializationLibrary" to "kotlinx_serialization",
            "dateLibrary" to "string",
            "sourceFolder" to "src/main/kotlin",
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(tasks.named("openApiGenerate"))
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.dotenv.kotlin)

    implementation(libs.koin.android)
}
