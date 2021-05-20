plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    implementation(project(":common:okhttp"))
    implementation(project(":common:statsd"))
    implementation(project(":common:time"))
    implementation(project(":logger:logger"))

    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:truth-extensions"))
    testImplementation(testFixtures(project(":common:statsd")))
    testImplementation(testFixtures(project(":logger:logger")))

    testFixturesApi(testFixtures(project(":common:statsd")))
    testFixturesApi(testFixtures(project(":logger:logger")))
    testFixturesApi(testFixtures(project(":common:time")))
}

kotlin {
    explicitApi()
}
