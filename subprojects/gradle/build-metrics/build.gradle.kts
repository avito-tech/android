plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":common:math"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:gradle-profile"))
    implementation(project(":gradle:critical-path:api"))
    implementation(project(":gradle:build-metrics-tracker"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:graphite-config"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:teamcity"))
    implementation(libs.kotlinPlugin)

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(testFixtures(project(":common:graphite")))
    testImplementation(testFixtures(project(":logger:logger")))
    testImplementation(testFixtures(project(":common:statsd")))
    testImplementation(testFixtures(project(":gradle:build-environment")))

    gradleTestImplementation(project(":common:junit-utils"))
    gradleTestImplementation(project(":common:test-okhttp"))
    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":gradle:git"))
    gradleTestImplementation(testFixtures(project(":logger:logger")))
}

gradlePlugin {
    plugins {
        create("buildMetrics") {
            id = "com.avito.android.build-metrics"
            implementationClass = "com.avito.android.plugin.build_metrics.BuildMetricsPlugin"
            displayName = "Build metrics"
        }
    }
}

kotlin {
    explicitApi()
}
