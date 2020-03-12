plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-shared-test"

dependencies {
    compileOnly(gradleApi())
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.test.junitJupiterApi)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.test.truth)
    implementation(project(":subprojects:gradle:runner:service"))
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation(project(":subprojects:gradle:test-project"))
}
