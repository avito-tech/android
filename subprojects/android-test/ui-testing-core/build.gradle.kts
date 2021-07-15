plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(libs.androidXTestCore)
    api(libs.espressoCore)
    api(libs.espressoWeb)
    api(libs.espressoIntents)
    api(libs.uiAutomator)
    api(libs.espressoDescendantActions)
    api(libs.appcompat)
    api(libs.recyclerView)
    api(libs.material)

    // todo implementation, waitForAssertion used in app
    api(projects.common.waiter)

    implementation(libs.bundles.hamcrest)
    implementation(libs.junit)
    implementation(libs.freeReflection)
    implementation(projects.androidTest.instrumentation)
}
