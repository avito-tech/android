plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val androidGradlePluginVersion: String by project

dependencies {
    api("com.android.tools.build:gradle:$androidGradlePluginVersion")

    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))

    testImplementation(testFixtures(project(":utils")))
}

//todo withSourcesJar 6.0 gradle
val sourcesTask = tasks.create<Jar>("sourceJar") {
    classifier = "sources"
    from(sourceSets.main.get().allJava)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourcesTask)
        }
    }
}
