plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    compileOnly(gradleApi())

    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.common.math)
    implementation(projects.subprojects.gradle.gradleExtensions)

    gradleTestImplementation(projects.subprojects.common.truthExtensions)
    gradleTestImplementation(testFixtures(projects.subprojects.logger.logger))
    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("moduleDependenciesGraph") {
            id = "com.avito.android.module-dependencies-graph"
            implementationClass = "com.avito.module.dependencies.ModuleDependenciesGraphPlugin"
            displayName = "Module dependencies graph"
            description = "Build module dependencies graph"
        }
    }
}
