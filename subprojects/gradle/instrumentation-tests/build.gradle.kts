plugins {
    id("convention.kotlin-jvm")
    `java-test-fixtures`
    id("convention.publish-gradle-plugin")
    id("convention.integration-testing")
    id("convention.libraries")
}

dependencies {
    api(project(":subprojects:gradle:kubernetes"))
    api(project(":subprojects:common:time"))
    api(project(":subprojects:gradle:build-verdict-tasks-api"))

    implementation(libs.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }
    implementation(libs.commonsText)
    implementation(libs.coroutinesCore)
    implementation(libs.funktionaleTry)
    implementation(libs.gson)
    implementation(libs.kotson)
    implementation(libs.retrofit)
    implementation(libs.teamcityClient)
    implementation(project(":subprojects:common:composite-exception"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:retrace"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:test-annotations"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:instrumentation-changed-tests-finder"))
    implementation(project(":subprojects:gradle:instrumentation-tests-dex-loader"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:runner:report"))
    implementation(project(":subprojects:gradle:runner:client"))
    implementation(project(":subprojects:gradle:runner:device-provider"))
    implementation(project(":subprojects:gradle:runner:stub"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:worker"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:slack-test-fixtures"))
    testImplementation(project(":subprojects:gradle:build-failer-test-fixtures"))
    testImplementation(project(":subprojects:gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(testFixtures(project(":subprojects:gradle:runner:report")))

    integTestImplementation(project(":subprojects:common:statsd"))

    testFixturesApi(testFixtures(project(":subprojects:common:logger")))
    testFixturesApi(testFixtures(project(":subprojects:common:time")))
    testFixturesApi(testFixtures(project(":subprojects:common:report-viewer")))
    testFixturesApi(testFixtures(project(":subprojects:gradle:runner:device-provider")))
    testFixturesApi(testFixtures(project(":subprojects:gradle:runner:report")))
}

kotlin {
    explicitApi()

    /**
     * Workaround to access internal classes from testFixtures
     * till https://youtrack.jetbrains.com/issue/KT-34901 resolved
     */
    target.compilations
        .matching { it.name in listOf("testFixtures") }
        .configureEach {
            associateWith(target.compilations.getByName("main"))
        }

    target.compilations
        .matching { it.name in listOf("integTest") }
        .configureEach {
            associateWith(target.compilations.getByName("testFixtures"))
        }

    target.compilations
        .matching { it.name in listOf("test") }
        .configureEach {
            associateWith(target.compilations.getByName("testFixtures"))
        }
}

gradlePlugin {
    plugins {
        create("functionalTests") {
            id = "com.avito.android.instrumentation-tests"
            implementationClass = "com.avito.instrumentation.InstrumentationTestsPlugin"
            displayName = "Instrumentation tests"
        }
    }
}
