plugins {
    `kotlin-dsl`
    id("convention.libraries")
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation("com.avito.android.buildlogic:libraries")
    implementation("com.avito.android.buildlogic:testing-convention")
    implementation(platform("com.avito.android.infra:platforms"))
    implementation(libs.kotlinPlugin)
    implementation(libs.nebulaIntegTest)
}

repositories {
    mavenCentral()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
