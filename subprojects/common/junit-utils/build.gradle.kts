plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.junit)
    implementation(libs.truth)
    implementation(libs.bundles.hamcrest)

    testImplementation(libs.kotlinReflect)
}

kotlin {
    explicitApiWarning()
}
