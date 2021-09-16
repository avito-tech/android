enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    includeBuild("../build-logic-settings")

    val artifactoryUrl: String? by settings

    repositories {
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
        exclusiveContent {
            forRepository {
                maven {
                    if (artifactoryUrl.isNullOrBlank()) {
                        name = "mavenCentral"
                        setUrl("https://repo1.maven.org/maven2")
                    } else {
                        name = "Proxy for mavenCentral: https://repo1.maven.org/maven2"
                        setUrl("$artifactoryUrl/mavenCentral")
                        isAllowInsecureProtocol = true
                    }
                }
            }
            filter {
                includeGroup("com.avito.android")
            }
        }
        maven {
            if (artifactoryUrl.isNullOrBlank()) {
                name = "google-android"
                setUrl("https://dl.google.com/dl/android/maven2/")
            } else {
                name = "Proxy for google-android: https://dl.google.com/dl/android/maven2/"
                setUrl("$artifactoryUrl/google-android")
                isAllowInsecureProtocol = true
            }
        }
    }

    val infraVersion = providers.systemProperty("infraVersion").forUseAtConfigurationTime()

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.avito.android") ->
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:${infraVersion.get()}")
            }
        }
    }
}

plugins {
    id("convention-scan")
    id("convention-cache")
    id("convention-dependencies")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../build-logic")

include(":gradle:artifactory-app-backup")
include(":gradle:artifactory-app-backup-test-fixtures")
include(":gradle:build-checks")
include(":gradle:build-metrics")
include(":gradle:build-metrics-tracker")
include(":gradle:critical-path:critical-path")
include(":gradle:critical-path:api")
include(":gradle:gradle-profile")
include(":gradle:build-properties")
include(":gradle:build-trace")
include(":gradle:build-verdict")
include(":gradle:build-verdict-tasks-api")
include(":gradle:cd")
include(":gradle:module-types")
include(":gradle:bitbucket")
include(":gradle:design-screenshots")
include(":gradle:prosector")
include(":gradle:code-ownership")
include(":gradle:pre-build")
include(":gradle:gradle-extensions")
include(":gradle:test-project")
include(":gradle:git")
include(":gradle:git-test-fixtures")
include(":gradle:impact-shared")
include(":gradle:impact-shared-test-fixtures")
include(":gradle:impact")
include(":gradle:sentry-config")
include(":gradle:graphite-config")
include(":gradle:statsd-config")
include(":gradle:android")
include(":gradle:lint-report")
include(":gradle:feature-toggles")
include(":gradle:ui-test-bytecode-analyzer")
include(":gradle:upload-cd-build-result")
include(":gradle:upload-to-googleplay")
include(":gradle:teamcity")
include(":gradle:qapps")
include(":gradle:tms")
include(":gradle:trace-event")
include(":gradle:process")
include(":gradle:test-summary")
include(":gradle:slack")
include(":gradle:slack-test-fixtures")
include(":gradle:build-failer")
include(":gradle:build-failer-test-fixtures")
include(":gradle:build-environment")
include(":gradle:worker")
include(":gradle:module-dependencies-graph")

include(":common:build-metadata")
include(":common:resources")
include(":common:files")
include(":common:time")
include(":common:okhttp")
include(":common:test-okhttp")
include(":common:result")
include(":common:elastic")
include(":common:http-client")
include(":common:sentry")
include(":common:graphite")
include(":common:statsd")
include(":common:statsd-test-fixtures")
include(":common:problem")
include(":common:waiter")
include(":common:kotlin-ast-parser")
include(":common:random-utils")
include(":common:teamcity-common")
include(":common:junit-utils")
include(":common:graph")
include(":common:math")
include(":common:retrace")
include(":common:truth-extensions")
include(":common:composite-exception")
include(":common:throwable-utils")
include(":common:coroutines-extension")

include(":android-test:resource-manager-exceptions")
include(":android-test:websocket-reporter")
include(":android-test:keep-for-testing")
include(":android-test:ui-testing-maps")
include(":android-test:ui-testing-core-app")
include(":android-test:ui-testing-core")
include(":android-test:instrumentation")
include(":android-test:toast-rule")
include(":android-test:snackbar-rule")
include(":android-test:test-screenshot")
include(":android-test:rx3-idler")

include(":android-lib:proxy-toast")
include(":android-lib:snackbar-proxy")

include(":test-runner:test-report-artifacts")
include(":test-runner:test-annotations")
include(":test-runner:test-report-api")
include(":test-runner:test-report-dsl-api")
include(":test-runner:test-report-dsl")
include(":test-runner:test-report")
include(":test-runner:test-inhouse-runner")
include(":test-runner:test-instrumentation-runner")
include(":test-runner:k8s-deployments-cleaner")
include(":test-runner:instrumentation-changed-tests-finder")
include(":test-runner:instrumentation-tests")
include(":test-runner:instrumentation-tests-dex-loader")
include(":test-runner:file-storage")
include(":test-runner:report-viewer")
include(":test-runner:report-processor")
include(":test-runner:report")
include(":test-runner:test-model")
include(":test-runner:runner-api")
include(":test-runner:client")
include(":test-runner:device-provider:model")
include(":test-runner:device-provider:impl")
include(":test-runner:device-provider:api")
include(":test-runner:service")
include(":test-runner:command-line-executor")
include(":test-runner:kubernetes")

include(":logger:gradle-logger")
include(":logger:android-log")
include(":logger:logger")
include(":logger:slf4j-logger")
include(":logger:sentry-logger")
include(":logger:elastic-logger")

include(":signer")

