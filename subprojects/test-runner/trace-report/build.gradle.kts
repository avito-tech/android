plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.common.time)

    implementation(projects.gradle.traceEvent)
    implementation(projects.testRunner.runnerApi)

    testFixturesApi(testFixtures(projects.testRunner.runnerApi))
}
