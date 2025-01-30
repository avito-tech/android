import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory
import com.avito.instrumentation.configuration.KubernetesViaCredentials
import com.avito.instrumentation.configuration.report.ReportConfig
import com.avito.instrumentation.reservation.request.Device
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import java.time.Duration

plugins {
    id("convention.kotlin-android-app")
    id("com.avito.android.instrumentation-tests")
}

android {

    namespace = "com.avito.android.ui"

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "com.avito.android.test.app.core.TestAppRunner"

        val instrumentationArgs = mapOf(
            "videoRecording" to "failed",
        )

        // These arguments are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments.putAll(instrumentationArgs)
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    packagingOptions {
        resources.pickFirsts.add("META-INF/okhttp.kotlin_module")
    }
}

dependencies {
    implementation(libs.androidXtracing) {
        because("androidx libs bring 1.0.0 instead of 1.1.0")
        because("https://github.com/android/android-test/issues/1755")
    }
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.playServicesBase)
    implementation(libs.recyclerView)
    implementation(libs.swipeRefreshLayout)

    implementation(project(":subprojects:android-lib:proxy-toast"))

    androidTestImplementation(project(":subprojects:test-runner:test-inhouse-runner"))
    androidTestImplementation(project(":subprojects:test-runner:test-report-android"))
    androidTestImplementation(project(":subprojects:test-runner:test-annotations"))
    androidTestImplementation(project(":subprojects:test-runner:report-viewer"))
    androidTestImplementation(project(":subprojects:android-test:ui-testing-core"))
    androidTestImplementation(project(":subprojects:android-test:toast-rule"))
    androidTestImplementation(project(":subprojects:common:truth-extensions"))

    androidTestUtil(libs.testOrchestrator)
}

val avitoRegistry = getOptionalStringProperty("avito.registry")

instrumentation {
    val reportKey = "avito.report.sender"
    val reportConfig = when (getOptionalStringProperty(reportKey, "noop")) {
        "noop" -> ReportConfig.NoOp
        "runner" -> ReportConfig.ReportViewer.SendFromRunner(
            reportApiUrl = getMandatoryStringProperty("avito.report.url"),
            reportViewerUrl = getMandatoryStringProperty("avito.report.viewerUrl"),
            fileStorageUrl = getMandatoryStringProperty("avito.fileStorage.url"),
            planSlug = "UiTestingCoreApp",
            jobSlug = "FunctionalTests"
        )

        else -> throw IllegalArgumentException("Invalid value for $reportKey property")
    }

    report.set(reportConfig)

    logcatTags = setOf(
        "UITestRunner:*",
        "ActivityManager:*",
        "ReportTestListener:*",
        "StorageJsonTransport:*",
        "TestReport:*",
        "VideoCaptureListener:*",
        "TestRunner:*",
        "SystemDialogsManager:*",
        "AndroidJUnitRunner:*",
        "ito.android.de:*", // по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
        "*:E"
    )

    filters {
        register("ci") {
            fromSource.excludeFlaky = true
            fromRunHistory.excludePreviousStatuses(
                setOf(
                    FromRunHistory.RunStatus.Success,
                    FromRunHistory.RunStatus.Manual
                )
            )
        }

        register("local") {
            fromSource
        }
    }

    environments {
        register<KubernetesViaCredentials>("kubernetes") {
            token.set(getMandatoryStringProperty("kubernetesToken"))
            url.set(getMandatoryStringProperty("kubernetesUrl"))
            namespace.set(getMandatoryStringProperty("kubernetesNamespace"))
        }
    }

    experimental {
        useLegacyExtensionsV1Beta.set(false)
    }

    val defaultCpuRequest = "1.15"
    val defaultCpuLimit = "1.3"
    val defaultMemoryLimit = "4Gi"

    val emulator27 = Device.CloudEmulator(
        name = "api27",
        api = 27,
        model = "Android_SDK_built_for_x86",
        image = emulatorImage(27, "32dcecb94ce8"),
        cpuCoresRequest = defaultCpuRequest,
        cpuCoresLimit = defaultCpuLimit,
        memoryLimit = defaultMemoryLimit
    )

    configurations {
        register("local") {
            filter = "local"
            targets {
                register("api34") {
                    deviceName = "API34"
                    scheduling {
                        quota {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }
                        testsCountBasedReservation {
                            device = Device.LocalEmulator.device(34, "sdk_gphone64_x86_64")
                            maximum = 1
                            testsPerEmulator = 1
                        }
                    }
                }
            }
        }

        register(
            "PRCheck",
            instrumentationConfiguration(
                targetDevices = setOf(emulator27),
            )
        )
    }
}

fun instrumentationConfiguration(
    targetDevices: Set<Device>,
): Action<InstrumentationConfiguration> {
    return Action {
        testRunnerExecutionTimeout = Duration.ofMinutes(10)
        instrumentationTaskTimeout = Duration.ofMinutes(10)
        reportSkippedTests = true
        filter = "ci"

        targetDevices.forEach { targetDevice ->
            targets {
                register(targetDevice.name) {
                    scheduling {
                        deviceName = targetDevice.name

                        quota {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }

                        testsCountBasedReservation {
                            device = targetDevice
                            minimum = 1
                            maximum = 5
                            testsPerEmulator = 12
                        }
                    }
                }
            }
        }
    }
}

fun emulatorImage(api: Int, label: String): String {
    return if (avitoRegistry != null) {
        "$avitoRegistry/android/emulator-hermetic-$api:$label"
    } else {
        "avitotech/android-emulator-$api:$label"
    }
}

val isLocalCheck = project.providers.gradleProperty("localCheck").getOrElse("false").toBoolean()

if (!isLocalCheck) {
    tasks.check {
        dependsOn(tasks.named("instrumentationPRCheckKubernetes"))
    }
}
