rootProject.name = "android"

include(":utils")
include(":git")
include(":kotlin-dsl-support")
include(":test-project")
include(":android")

pluginManagement {

    repositories {
        jcenter()
        @Suppress("UnstableApiUsage")
        gradlePluginPortal()
        google()
    }

    val kotlinVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            }
        }
    }
}
