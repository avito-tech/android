plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    kotlin("kapt") // TODO replace with ksp
    application
}

dependencies {
    implementation(projects.subprojects.emcee.queueWorkerApi)
    implementation(projects.subprojects.emcee.androidDevice)
    implementation(libs.kotlinXCli)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.moshi)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    kapt(libs.moshiKapt)

    testImplementation(libs.truth)
}

application {
    mainClass.set("com.avito.emcee.worker.WorkerMain")
}

tasks {
    val fatJarTask = register<Jar>("fatJar") {
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass))
        }
        // TODO: use DuplicatesStrategy.FAIL instead
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
            sourcesMain.output

        from(contents)
        dependsOn("assemble")
    }
    build {
        dependsOn(fatJarTask)
    }
}
