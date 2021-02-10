plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:gradle:build-environment")) {
        because("project.buildEnvironment only")
    }
    api(project(":subprojects:gradle:process"))

    implementation(gradleApi())
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:slf4j-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
}
