plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.kotlin)
    implementation(projects.gradleExt)
    implementation(projects.dependencyLocking)
    implementation(libs.androidGradle)
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        create("android-lib") {
            id = "convention.kotlin-android-library"
            implementationClass = "com.avito.android.AndroidLibraryPlugin"
        }

        create("android-app") {
            id = "convention.kotlin-android-app"
            implementationClass = "com.avito.android.AndroidAppPlugin"
        }
    }
}
