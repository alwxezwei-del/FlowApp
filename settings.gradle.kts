pluginManagement {
    includeBuild("build-logic")
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
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FlowApp"

include(":app")

include(":core:model")
include(":core:common")
include(":core:domain")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:ui")

include(":feature:home")
include(":feature:tasks")
include(":feature:habits")
include(":feature:focus")
include(":feature:statistics")
include(":feature:history")
include(":feature:settings")
