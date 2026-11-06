pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Lbsuhack"
include(":app")

include(":core:designsystem")
include(":core:navigation")
include(":core:domain")
include(":core:data")
include(":core:localization")
include(":core:network")
include(":core:ui")
include(":layers")

include(":feature:activities")
include(":feature:activevote")
include(":feature:auth")
include(":feature:events")
include(":feature:profile")
include(":feature:requests")
