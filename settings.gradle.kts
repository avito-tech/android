rootProject.name = "android"

enableFeaturePreview("GRADLE_METADATA")

include(":logging")
include(":utils")
include(":git")
include(":kotlin-dsl-support")
include(":sentry")
include(":okhttp")
include(":docker")
include(":trace-event")
include(":test-okhttp")
include(":test-project")
include(":impact")
include(":android")
include(":time")

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
