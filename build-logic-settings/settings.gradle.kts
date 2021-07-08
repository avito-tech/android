enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "build-logic-settings"

dependencyResolutionManagement {
    repositories {

        val artifactoryUrl: String? by settings

        maven {
            if (artifactoryUrl.isNullOrBlank()) {
                name = "gradle-plugins"
                setUrl("https://plugins.gradle.org/m2/")
            } else {
                name = "Proxy for gradle-plugins: https://plugins.gradle.org/m2/"
                setUrl("$artifactoryUrl/gradle-plugins")
                isAllowInsecureProtocol = true
            }
        }
    }
}

include("cache-plugin")
include("dependency-plugin")
include("scan-plugin")
