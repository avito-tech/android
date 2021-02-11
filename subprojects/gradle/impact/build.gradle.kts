plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:gradle:impact-shared"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:statsd-config"))

    implementation(Dependencies.antPattern)
    implementation(Dependencies.Gradle.kotlinPlugin)

    testImplementation(project(":subprojects:gradle:impact-shared-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:git-test-fixtures"))
    testImplementation(Dependencies.Test.mockitoKotlin)
}

gradlePlugin {
    plugins {
        create("impact") {
            id = "com.avito.android.impact"
            implementationClass = "com.avito.impact.plugin.ImpactAnalysisPlugin"
            displayName = "Impact analysis"
        }
    }
}
