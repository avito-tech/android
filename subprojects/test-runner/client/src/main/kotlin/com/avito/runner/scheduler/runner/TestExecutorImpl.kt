package com.avito.runner.scheduler.runner

import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.model.ReservationData
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.config.QuotaConfigurationData
import com.avito.runner.config.Reservation
import com.avito.runner.scheduler.TestRunnerFactory
import com.avito.runner.scheduler.args.TestRunnerFactoryConfig
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestWithTarget
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

internal class TestExecutorImpl(
    private val devicesProvider: DevicesProvider,
    private val configurationName: String,
    testReporter: TestLifecycleListener,
    loggerFactory: LoggerFactory,
    metricsConfig: RunnerMetricsConfig,
    saveTestArtifactsToOutputs: Boolean,
    fetchLogcatForIncompleteTests: Boolean,
) : TestExecutor {

    private val logger = loggerFactory.create<TestExecutor>()

    private val factory = TestRunnerFactory(
        TestRunnerFactoryConfig(
            loggerFactory = loggerFactory,
            listener = testReporter,
            reservation = devicesProvider,
            metricsConfig = metricsConfig,
            saveTestArtifactsToOutputs = saveTestArtifactsToOutputs,
            fetchLogcatForIncompleteTests = fetchLogcatForIncompleteTests,
        )
    )

    private val outputDirectoryName = "test-runner"

    private val scope = CoroutineScope(CoroutineName("test-executor") + Dispatchers.IO)

    override fun execute(
        application: File?,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        output: File
    ) {
        withDevices(reservations = reservations(testsToRun)) { devices ->

            val testRequests = testsToRun.map { targetTestRun: TestWithTarget ->

                val reservation = targetTestRun.target.reservation

                createTestRunRequest(
                    targetTestRun = targetTestRun,
                    quota = reservation.quota,
                    reservation = reservation,
                    application = application,
                    testApplication = testApplication,
                    executionParameters = executionParameters
                )
            }

            runBlocking {
                withContext(scope.coroutineContext) {
                    factory.createTestRunner(
                        outputDirectory = outputFolder(output),
                        devices = devices
                    ).runTests(testRequests)
                }
            }
        }

        logger.debug("Worker completed")
    }

    private fun outputFolder(output: File): File = File(
        output,
        outputDirectoryName
    ).apply { mkdirs() }

    private fun reservations(
        tests: List<TestWithTarget>
    ): Collection<ReservationData> {

        val testsGroupedByTargets: Map<TargetGroup, List<TestWithTarget>> = tests.groupBy {
            TargetGroup(
                name = it.target.name,
                reservation = it.target.reservation
            )
        }

        return testsGroupedByTargets
            .map { (target, tests) ->
                val reservation = target.reservation.data(
                    tests = tests.map { it.test.name }
                )

                logger.info(
                    "Devices: ${reservation.count} devices will be allocated for " +
                        "target: ${target.name} inside configuration: $configurationName"
                )

                reservation
            }
    }

    // TODO: extract and delegate this channels orchestration.
    // It's overcomplicated for local client
    private fun withDevices(
        reservations: Collection<ReservationData>,
        action: (devices: ReceiveChannel<Device>) -> Unit
    ) {
        runBlocking {
            scope.launch {
                try {
                    logger.info("Devices: Starting action job for configuration: $configurationName...")
                    action(devicesProvider.provideFor(reservations, this))
                    logger.info("Devices: Action completed for configuration: $configurationName")
                } catch (e: Throwable) {
                    logger.critical("Error during action in $configurationName job", e)
                } finally {
                    withContext(NonCancellable) {
                        logger.info("Devices: Starting releasing devices for configuration: $configurationName...")
                        devicesProvider.releaseDevices()
                        logger.info("Devices: Devices released for configuration: $configurationName")
                    }
                }
            }.join()
        }
    }

    private fun createTestRunRequest(
        targetTestRun: TestWithTarget,
        quota: QuotaConfigurationData,
        reservation: Reservation,
        application: File?,
        testApplication: File,
        executionParameters: ExecutionParameters
    ): TestRunRequest = TestRunRequest(
        testCase = TestCase(
            className = targetTestRun.test.name.className,
            methodName = targetTestRun.test.name.methodName,
            deviceName = targetTestRun.target.deviceName
        ),
        configuration = DeviceConfiguration(
            api = reservation.device.api,
            model = reservation.device.model
        ),
        scheduling = TestRunRequest.Scheduling(
            retryCount = quota.retryCount,
            minimumFailedCount = quota.minimumFailedCount,
            minimumSuccessCount = quota.minimumSuccessCount
        ),
        application = application?.absolutePath,
        applicationPackage = executionParameters.applicationPackageName,
        testApplication = testApplication.absolutePath,
        testPackage = executionParameters.applicationTestPackageName,
        testRunner = executionParameters.testRunner,
        timeoutMinutes = TEST_TIMEOUT_MINUTES,
        instrumentationParameters = targetTestRun.target.instrumentationParams,
        enableDeviceDebug = executionParameters.enableDeviceDebug
    )

    data class TargetGroup(val name: String, val reservation: Reservation)
}

private const val TEST_TIMEOUT_MINUTES = 5L