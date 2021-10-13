enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "avito-android-infra"

pluginManagement {
    includeBuild("build-logic-settings")
}

plugins {
    id("convention-plugins")
    id("convention-scan")
    id("convention-cache")
    id("convention-dependencies")
}

includeBuild("build-logic")

include(":subprojects:gradle:artifactory-app-backup")
include(":subprojects:gradle:artifactory-app-backup-test-fixtures")
include(":subprojects:gradle:build-checks")
include(":subprojects:gradle:build-metrics")
include(":subprojects:gradle:build-metrics-tracker")
include(":subprojects:gradle:critical-path:critical-path")
include(":subprojects:gradle:critical-path:api")
include(":subprojects:gradle:gradle-profile")
include(":subprojects:gradle:build-properties")
include(":subprojects:gradle:build-trace")
include(":subprojects:gradle:build-verdict")
include(":subprojects:gradle:build-verdict-tasks-api")
include(":subprojects:gradle:cd")
include(":subprojects:gradle:module-types")
include(":subprojects:gradle:bitbucket")
include(":subprojects:gradle:design-screenshots")
include(":subprojects:gradle:prosector")
include(":subprojects:gradle:code-ownership")
include(":subprojects:gradle:pre-build")
include(":subprojects:gradle:gradle-extensions")
include(":subprojects:gradle:test-project")
include(":subprojects:gradle:git")
include(":subprojects:gradle:git-test-fixtures")
include(":subprojects:gradle:impact-shared")
include(":subprojects:gradle:impact-shared-test-fixtures")
include(":subprojects:gradle:impact")
include(":subprojects:gradle:sentry-config")
include(":subprojects:gradle:graphite-config")
include(":subprojects:gradle:statsd-config")
include(":subprojects:gradle:android")
include(":subprojects:gradle:feature-toggles")
include(":subprojects:gradle:ui-test-bytecode-analyzer")
include(":subprojects:gradle:upload-cd-build-result")
include(":subprojects:gradle:upload-to-googleplay")
include(":subprojects:gradle:teamcity")
include(":subprojects:gradle:qapps")
include(":subprojects:gradle:tms")
include(":subprojects:gradle:trace-event")
include(":subprojects:gradle:process")
include(":subprojects:gradle:test-summary")
include(":subprojects:gradle:slack")
include(":subprojects:gradle:slack-test-fixtures")
include(":subprojects:gradle:build-failer")
include(":subprojects:gradle:build-failer-test-fixtures")
include(":subprojects:gradle:build-environment")
include(":subprojects:gradle:worker")
include(":subprojects:gradle:module-dependencies-graph")

include(":subprojects:common:build-metadata")
include(":subprojects:common:resources")
include(":subprojects:common:files")
include(":subprojects:common:time")
include(":subprojects:common:okhttp")
include(":subprojects:common:test-okhttp")
include(":subprojects:common:result")
include(":subprojects:common:elastic")
include(":subprojects:common:http-client")
include(":subprojects:common:sentry")
include(":subprojects:common:graphite")
include(":subprojects:common:statsd")
include(":subprojects:common:statsd-test-fixtures")
include(":subprojects:common:problem")
include(":subprojects:common:waiter")
include(":subprojects:common:kotlin-ast-parser")
include(":subprojects:common:random-utils")
include(":subprojects:common:teamcity-common")
include(":subprojects:common:junit-utils")
include(":subprojects:common:graph")
include(":subprojects:common:math")
include(":subprojects:common:retrace")
include(":subprojects:common:truth-extensions")
include(":subprojects:common:composite-exception")
include(":subprojects:common:throwable-utils")
include(":subprojects:common:coroutines-extension")

include(":subprojects:android-test:resource-manager-exceptions")
include(":subprojects:android-test:websocket-reporter")
include(":subprojects:android-test:keep-for-testing")
include(":subprojects:android-test:ui-testing-core-app")
include(":subprojects:android-test:ui-testing-core")
include(":subprojects:android-test:instrumentation")
include(":subprojects:android-test:toast-rule")
include(":subprojects:android-test:snackbar-rule")
include(":subprojects:android-test:test-screenshot")
include(":subprojects:android-test:rx3-idler")

include(":subprojects:android-lib:proxy-toast")
include(":subprojects:android-lib:snackbar-proxy")

include(":subprojects:test-runner:test-report-artifacts")
include(":subprojects:test-runner:test-annotations")
include(":subprojects:test-runner:test-report-api")
include(":subprojects:test-runner:test-report-dsl-api")
include(":subprojects:test-runner:test-report-dsl")
include(":subprojects:test-runner:test-report")
include(":subprojects:test-runner:test-inhouse-runner")
include(":subprojects:test-runner:test-instrumentation-runner")
include(":subprojects:test-runner:k8s-deployments-cleaner")
include(":subprojects:test-runner:instrumentation-changed-tests-finder")
include(":subprojects:test-runner:instrumentation-tests")
include(":subprojects:test-runner:instrumentation-tests-dex-loader")
include(":subprojects:test-runner:file-storage")
include(":subprojects:test-runner:report-viewer")
include(":subprojects:test-runner:report-processor")
include(":subprojects:test-runner:report")
include(":subprojects:test-runner:test-model")
include(":subprojects:test-runner:runner-api")
include(":subprojects:test-runner:client")
include(":subprojects:test-runner:device-provider:model")
include(":subprojects:test-runner:device-provider:impl")
include(":subprojects:test-runner:device-provider:api")
include(":subprojects:test-runner:service")
include(":subprojects:test-runner:command-line-executor")
include(":subprojects:test-runner:kubernetes")

include(":subprojects:logger:gradle-logger")
include(":subprojects:logger:android-log")
include(":subprojects:logger:logger")
include(":subprojects:logger:slf4j-logger")
include(":subprojects:logger:sentry-logger")
include(":subprojects:logger:elastic-logger")

include(":subprojects:signer")
