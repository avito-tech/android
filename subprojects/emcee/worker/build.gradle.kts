plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    kotlin("kapt") // TODO replace with ksp
    application
}

dependencies {
    implementation(libs.kotlinXCli)
    implementation(projects.subprojects.emcee.queueWorkerApi)
    implementation(projects.subprojects.emcee.androidDevice)
    implementation(libs.coroutinesCore)
    implementation(libs.moshi)
    kapt(libs.moshiKapt)

    testImplementation(libs.truth)
}

application {
    mainClass.set("com.avito.emcee.worker.WorkerMain")
}