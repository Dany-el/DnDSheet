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
        maven("https://chaquo.com/maven")
    }
    plugins {
        kotlin("jvm") version "2.3.10"
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
        maven("https://chaquo.com/maven")
    }
}

rootProject.name = "D&D Sheet"
include(":app")
include(":core")
include(":feature")
include(":core:data")
include(":core:ui")
include(":feature:character")
include(":feature:compendium")
include(":feature:settings")
include(":feature:wizard")
include(":feature:character-spells")
include(":core:domain")
include(":core:pdf")
include(":core:model")
include(":core:navigation")
include(":core:dice")