import java.util.Properties

plugins {
    id("android.dev.library")
    id("tech.dev.ktor")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.openapi.generator)
}

// Build-time configuration. Runtime dotenv is not available on Android because
// `.env` is not packaged into the APK. Precedence: Gradle property > .env > local.properties > default.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val dotEnvValues: Map<String, String> =
    rootProject.file(".env")
        .takeIf { it.exists() }
        ?.readLines()
        ?.map(String::trim)
        ?.filter { it.isNotEmpty() && !it.startsWith("#") }
        ?.mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) {
                null
            } else {
                line.substring(0, separator).trim() to
                    line.substring(separator + 1)
                        .trim()
                        .removeSurrounding("\"")
                        .removeSurrounding("'")
            }
        }
        ?.toMap()
        .orEmpty()

val configuredBaseUrl: String? = providers.gradleProperty("BASE_URL").orNull
    ?: dotEnvValues["BASE_URL"]
    ?: localProperties.getProperty("BASE_URL")

val baseUrl = configuredBaseUrl?.takeIf { it.isNotBlank() } ?: "https://api.kola6.online/"

if (configuredBaseUrl.isNullOrBlank()) {
    logger.warn("BASE_URL is not configured. Falling back to $baseUrl")
}

val generatedOpenApiDir = layout.buildDirectory.dir("generated/openapi")

android {
    namespace = "com.dev.lbsuhack.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField(
            "String",
            "BASE_URL",
            "\"${baseUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
    }

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
    typeMappings.set(mapOf("UUID" to "kotlin.String"))
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

    implementation(libs.koin.android)

    testImplementation(libs.bundles.unit.test)
    testImplementation("io.ktor:ktor-client-mock:3.0.3")
}
