import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.kubernetesCredentials

plugins {
    id("convention.kotlin-android-app")
    id("com.avito.android.instrumentation-tests")
}

android {
    defaultConfig {
        testInstrumentationRunner = "com.avito.android.test.TestRunner"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    packagingOptions {
        pickFirst("META-INF/okhttp.kotlin_module")
    }
}

dependencies {
    androidTestImplementation("com.avito.android:test-inhouse-runner")
    androidTestImplementation("com.avito.android:test-report")
    androidTestImplementation("com.avito.android:logger")
    androidTestImplementation(libs.truth)

    androidTestUtil(libs.testOrchestrator)
}

instrumentation {
    sentryDsn = getOptionalStringProperty("avito.instrumentaion.sentry.dsn") ?: "http://stub-project@stub-host/0"

    filters.register("ci") {
        fromSource.excludeFlaky = true
    }

    experimental {
        useInMemoryReport.set(true)
        fetchLogcatForIncompleteTests.set(true)
        saveTestArtifactsToOutputs.set(true)
    }

    val credentials = project.kubernetesCredentials
    if (credentials is KubernetesCredentials.Service || credentials is KubernetesCredentials.Config) {

        val emulator29 = CloudEmulator(
            name = "api29",
            api = 29,
            model = "Android_SDK_built_for_x86_64",
            image = emulatorImage(29, "915c1f20be"),
            cpuCoresRequest = "1",
            cpuCoresLimit = "1.3",
            memoryLimit = "4Gi"
        )

        configurations {
            register("ui") {
                reportSkippedTests = true
                filter = "ci"

                kubernetesNamespace = "android-emulator"

                targets {
                    register("api29") {
                        deviceName = "API29"

                        scheduling {
                            quota {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            testsCountBasedReservation {
                                device = emulator29
                                maximum = 1
                                minimum = 1
                                testsPerEmulator = 1
                            }
                        }
                    }
                }
            }
        }
    }
}

val avitoRegistry = getOptionalStringProperty("avito.registry")

fun emulatorImage(api: Int, label: String): String {
    return if (avitoRegistry != null) {
        "$avitoRegistry/android/emulator-$api:$label"
    } else {
        "avitotech/android-emulator-$api:$label"
    }
}
