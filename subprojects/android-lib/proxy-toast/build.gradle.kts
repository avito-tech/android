plugins {
    id("com.android.library")
    id("kotlin-android")
    id("digital.wup.android-maven-publish")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation("androidx.annotation:annotation:1.1.0")
}
