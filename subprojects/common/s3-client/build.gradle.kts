plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:common:result"))
    implementation(libs.awsS3)
}
