plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-service")
}

dependencies {
    api(projects.common.coroutinesExtension)
    api(projects.common.statsd)
    api(projects.common.time)
    api(projects.gradle.process)
    api(projects.testRunner.commandLineExecutor)
    api(projects.testRunner.deviceProvider.api)
    api(projects.testRunner.testModel)
    api(projects.testRunner.runnerApi)

    implementation(projects.common.result)
    implementation(projects.common.problem)
    implementation(projects.testRunner.testReportArtifacts) {
        because("DeviceWorker pulls test artifacts")
    }
    implementation(libs.ddmlib)
    implementation(libs.rxJava)
    implementation(libs.kotlinStdlibJdk7) {
        because("java.nio.file.Path extensions")
    }

    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.common.time))
    testImplementation(testFixtures(projects.testRunner.runnerApi))
    testImplementation(projects.common.files)
    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.common.resources)
    testImplementation(libs.coroutinesTest)
}
