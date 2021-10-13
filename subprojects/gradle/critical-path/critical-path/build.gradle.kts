plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(projects.subprojects.gradle.criticalPath.api)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(libs.gson)
    implementation(projects.subprojects.gradle.gradleExtensions)

    gradleTestImplementation(projects.subprojects.common.junitUtils)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(testFixtures(projects.subprojects.logger.logger))
}

gradlePlugin {
    plugins {
        create("criticalPath") {
            id = "com.avito.android.critical-path"
            implementationClass = "com.avito.android.critical_path.CriticalPathPlugin"
            displayName = "Build critical path"
            description = "Calculates critical path of a build. These are tasks that define build duration."
        }
    }
}
