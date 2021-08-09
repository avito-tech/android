package com.avito.runner.scheduler.runner

import com.avito.android.Result
import com.avito.android.runner.devices.StubDevicesProvider
import com.avito.coroutines.extensions.Dispatchers
import com.avito.logger.StubLoggerFactory
import com.avito.runner.config.QuotaConfigurationData
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.config.createStubInstance
import com.avito.runner.model.TestCaseRun
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.metrics.StubTestMetricsListener
import com.avito.runner.scheduler.report.CompositeReporter
import com.avito.runner.scheduler.report.SummaryReportMakerImpl
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.scheduler.runner.model.TestRunnerResult
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.listener.NoOpTestListener
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.Device.DeviceStatus
import com.avito.runner.service.worker.device.Device.Signal
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.createStubInstance
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.service.worker.device.stub.StubActionResult
import com.avito.runner.service.worker.device.stub.StubDevice
import com.avito.runner.service.worker.device.stub.StubDevice.Companion.installApplicationFailure
import com.avito.runner.service.worker.device.stub.StubDevice.Companion.installApplicationSuccess
import com.avito.runner.service.worker.listener.StubDeviceListener
import com.avito.runner.trace.StubTraceReporter
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.test.model.TestName
import com.avito.time.StubTimeProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
internal class RunnerIntegrationTest {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private val devices = Channel<Device>(Channel.UNLIMITED)
    private val state = TestRunnerExecutionState()
    private val deviceModel = "stub"
    private val deviceName = DeviceName("api22")
    private val deviceApi = 22
    private val defaultQuota = QuotaConfigurationData(
        retryCount = 0,
        minimumSuccessCount = 1,
        minimumFailedCount = 0
    )
    private val loggerFactory = StubLoggerFactory

    @TempDir
    lateinit var outputDirectory: File

    @Test
    fun `all tests passed - for 1 successful device`() =
        runBlockingTest {
            val targets = listOf(createTarget())
            val runner = provideRunner(targets)
            val tests = createTests(count = 4)
            val device = createSuccessfulDevice(tests.size)

            devices.send(device)
            val result = runner.runTests(tests = tests)

            device.verify()
            assertRunTestResult(
                runnerResult = result.getOrThrow(),
                expected = tests.map { test ->
                    ExpectedTestResult(
                        testCase = test,
                        deviceTestCaseRuns = listOf(test.toPassedRun(device))
                    )
                }
            )
        }

    @Suppress("MaxLineLength")
    @Test
    fun `all tests passed by first and second devices - first device completes half of tests and fails, second connects later and completes all remaining tests`() =
        runBlockingTest {
            val targets = listOf(createTarget())
            val runner = provideRunner(targets)
            val tests = createTests(count = 4)

            val firstFailedDevice = StubDevice(
                loggerFactory = loggerFactory,
                model = deviceModel,
                apiResult = StubActionResult.Success(deviceApi),
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                installApplicationResults = mutableListOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive, // Device status for initializing
                    DeviceStatus.Alive, // Device status for first test
                    DeviceStatus.Alive, // Device status for second test
                    deviceIsFreezed() // Device status for third test
                ),
                // First request doesn't need clearing package
                // Third test has freeze status
                // Last request executing on another device
                clearPackageResults = (0 until tests.size - 3).flatMap {
                    listOf(
                        succeedClearPackage(),
                        succeedClearPackage()
                    )
                },
                runTestsResults = listOf(
                    testPassed(), // Test result for first test
                    testPassed() // Test result for second test
                )
            )
            val secondDevice = StubDevice(
                loggerFactory = loggerFactory,
                model = deviceModel,
                apiResult = StubActionResult.Success(deviceApi),
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                installApplicationResults = mutableListOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                clearPackageResults = listOf(
                    succeedClearPackage(),
                    succeedClearPackage()
                ),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive, // Device status for initializing
                    DeviceStatus.Alive, // Device status for third test
                    DeviceStatus.Alive // Device status for fourth test
                ),
                runTestsResults = listOf(
                    testPassed(), // Test result for third test
                    testPassed() // Test result for fourth test
                )
            )

            launch {
                devices.send(firstFailedDevice)
                // Wait for completion 2 tests by first device
                while (!firstFailedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(secondDevice)
            }

            val result = runner.runTests(tests)
            firstFailedDevice.verify()
            secondDevice.verify()

            val resultsByFirstDevice = tests.slice(0..1).map { test ->
                ExpectedTestResult(
                    testCase = test,
                    deviceTestCaseRuns = listOf(test.toPassedRun(firstFailedDevice))
                )
            }
            val resultsBySecondDevice = tests.slice(2..3).map { test ->
                ExpectedTestResult(
                    testCase = test,
                    deviceTestCaseRuns = listOf(test.toPassedRun(secondDevice))
                )
            }

            assertRunTestResult(
                runnerResult = result.getOrThrow(),
                expected = resultsByFirstDevice + resultsBySecondDevice
            )
        }

    @Test
    fun `all tests passed by first device - for 1 successful and 1 freeze device`() =
        runBlockingTest {
            val targets = listOf(createTarget())
            val runner = provideRunner(targets)

            val tests = createTests(count = 4)

            val successfulDevice = createSuccessfulDevice(tests.size)
            val failedDevice = createBrokenDevice(Exception())

            launch {
                devices.send(failedDevice)
                // Wait for completion 2 tests by first device
                while (!failedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(successfulDevice)
            }

            val result = runner.runTests(tests)
            successfulDevice.verify()
            failedDevice.verify()

            assertRunTestResult(
                runnerResult = result.getOrThrow(),
                expected = tests.map { test ->
                    ExpectedTestResult(
                        testCase = test,
                        deviceTestCaseRuns = listOf(test.toPassedRun(successfulDevice))
                    )
                }
            )
        }

    @Test
    fun `all tests passed by first device - for 1 successful and 1 failed to get status device`() =
        runBlockingTest {
            val targets = listOf(createTarget())
            val runner = provideRunner(targets)

            val tests = createTests(count = 4)

            val successfulDevice = createSuccessfulDevice(tests.size)
            val failedDevice = createBrokenDevice(Exception())

            launch {
                devices.send(failedDevice)
                while (!failedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(successfulDevice)
            }

            val result = runner.runTests(tests)
            successfulDevice.verify()
            failedDevice.verify()

            assertRunTestResult(
                runnerResult = result.getOrThrow(),
                expected = tests.map { test ->
                    ExpectedTestResult(
                        testCase = test,
                        deviceTestCaseRuns = listOf(test.toPassedRun(successfulDevice))
                    )
                }
            )
        }

    @Test
    fun `all tests passed by first device - when second device failed on application installing`() =
        runBlockingTest {
            val targets = listOf(createTarget())
            val runner = provideRunner(targets)

            val tests = createTests(count = 4)

            val successfulDevice = createSuccessfulDevice(tests.size)
            val failedDevice = StubDevice(
                tag = "StubDevice:installProblems",
                loggerFactory = loggerFactory,
                model = deviceModel,
                apiResult = StubActionResult.Success(deviceApi),
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive,
                    DeviceStatus.Alive
                ),
                installApplicationResults = mutableListOf(
                    installApplicationFailure(), // Install test application
                    installApplicationFailure() // Install application
                )
            )

            launch {
                devices.send(failedDevice)
                while (!failedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(successfulDevice)
            }

            val result = runner.runTests(tests)
            successfulDevice.verify()
            failedDevice.verify()

            assertRunTestResult(
                runnerResult = result.getOrThrow(),
                expected = tests.map { test ->
                    ExpectedTestResult(
                        testCase = test,
                        deviceTestCaseRuns = listOf(test.toPassedRun(successfulDevice))
                    )
                }
            )
        }

    @Test
    fun `test passed after retry of failed test`() = runBlockingTest {
        val targets = listOf(
            createTarget(
                quota = QuotaConfigurationData(
                    retryCount = 1,
                    minimumSuccessCount = 1,
                    minimumFailedCount = 0
                )
            )
        )
        val runner = provideRunner(targets)
        val tests = createTests(count = 2)

        val device = StubDevice(
            loggerFactory = loggerFactory,
            model = deviceModel,
            apiResult = StubActionResult.Success(deviceApi),
            coordinate = DeviceCoordinate.Local.createStubInstance(),
            installApplicationResults = listOf(
                installApplicationSuccess(), // Install application
                installApplicationSuccess() // Install test application
            ),
            clearPackageResults = listOf(
                succeedClearPackage(), // Clear test package for first try for second test
                succeedClearPackage(), // Clear application package for first try for second test

                succeedClearPackage(), // Clear test package for second try for second test
                succeedClearPackage() // Clear application package for second try for second test
            ),
            gettingDeviceStatusResults = listOf(
                DeviceStatus.Alive, // Alive status for initializing
                DeviceStatus.Alive, // Alive status for first test
                DeviceStatus.Alive, // Alive status for first try for second test
                DeviceStatus.Alive // Alive status for second test try for second test
            ),
            runTestsResults = listOf(
                testPassed(), // First test passed
                testFailed(), // Second test failed
                testPassed() // Second test passed by second try
            )
        )

        devices.send(device)

        val result = runner.runTests(tests)
        device.verify()

        val firstTestResult = tests.slice(listOf(0)).map { test ->
            ExpectedTestResult(
                testCase = test,
                deviceTestCaseRuns = listOf(test.toPassedRun(device))
            )
        }

        val secondTestResult = tests.slice(listOf(1)).map { test ->
            ExpectedTestResult(
                testCase = test,
                deviceTestCaseRuns = listOf(test.toFailedRun(device), test.toPassedRun(device))
            )
        }

        assertRunTestResult(
            runnerResult = result.getOrThrow(),
            expected = firstTestResult + secondTestResult
        )
    }

    @Test
    fun `test passed after retry of failed test when minimal passed count is 2 and retry quota is 4`() =
        runBlockingTest {
            val targets = listOf(
                createTarget(
                    quota = QuotaConfigurationData(
                        retryCount = 4,
                        minimumSuccessCount = 2,
                        minimumFailedCount = 0
                    )
                )
            )
            val runner = provideRunner(targets)
            val tests = createTests(count = 2)

            val device = StubDevice(
                loggerFactory = loggerFactory,
                model = deviceModel,
                apiResult = StubActionResult.Success(deviceApi),
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                installApplicationResults = listOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive, // Alive status for initializing
                    DeviceStatus.Alive, // Alive status for first try for first test
                    DeviceStatus.Alive, // Alive status for second try for first test
                    DeviceStatus.Alive, // Alive status for first try for second test
                    DeviceStatus.Alive, // Alive status for second try for second test
                    DeviceStatus.Alive, // Alive status for third try for first test
                    DeviceStatus.Alive, // Alive status for third try for second test
                    DeviceStatus.Alive // Alive status for fourth try for second test
                ),
                clearPackageResults = listOf(
                    succeedClearPackage(), // Clear test package for second try for first test
                    succeedClearPackage(), // Clear application package for second try for first test
                    succeedClearPackage(), // Clear test package for first try for second test
                    succeedClearPackage(), // Clear application package for first try for second test
                    succeedClearPackage(), // Clear test package for second try for second test
                    succeedClearPackage(), // Clear application package for second try for second test
                    succeedClearPackage(), // Clear test package for first try for third test
                    succeedClearPackage(), // Clear application package for first try for third test
                    succeedClearPackage(), // Clear test package for second try for third test
                    succeedClearPackage(), // Clear application package for second try for third test
                    succeedClearPackage(), // Clear test package for second try for fourth test
                    succeedClearPackage() // Clear application package for second try for fourth test
                ),
                runTestsResults = listOf(
                    testPassed(), // First test passed by first try
                    testFailed(), // First test failed by second try
                    testPassed(), // First test passed  by third try
                    testFailed(), // Second test failed by first try
                    testPassed(), // Second test passed by second try
                    testFailed(), // Second test failed by third try
                    testPassed() // Second test passed by fourth try
                )
            )

            devices.send(device)

            val result = runner.runTests(tests)
            device.verify()

            val firstTestResult = tests.slice(listOf(0)).map { test ->
                ExpectedTestResult(
                    testCase = test,
                    deviceTestCaseRuns = listOf(
                        test.toPassedRun(device),
                        test.toFailedRun(device),
                        test.toPassedRun(device)
                    )
                )
            }

            val secondTestResult = tests.slice(listOf(1)).map { test ->
                ExpectedTestResult(
                    testCase = test,
                    deviceTestCaseRuns = listOf(
                        test.toFailedRun(device),
                        test.toPassedRun(device),
                        test.toFailedRun(device),
                        test.toPassedRun(device)
                    )
                )
            }

            assertRunTestResult(
                runnerResult = result.getOrThrow(),
                expected = firstTestResult + secondTestResult
            )
        }

    @Test
    fun `test completed after 1 success and 1 fail for that requirements with retryCount 4`() =
        runBlockingTest {
            val targets = listOf(
                createTarget(
                    quota = QuotaConfigurationData(
                        retryCount = 4,
                        minimumSuccessCount = 1,
                        minimumFailedCount = 1
                    )
                )
            )
            val runner = provideRunner(targets)
            val tests = createTests(count = 2)

            val device = StubDevice(
                loggerFactory = loggerFactory,
                model = deviceModel,
                apiResult = StubActionResult.Success(deviceApi),
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                installApplicationResults = listOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive, // Alive status for initializing
                    DeviceStatus.Alive, // Alive status for first try for first test
                    DeviceStatus.Alive, // Alive status for second try for first test
                    DeviceStatus.Alive, // Alive status for first try for second test
                    DeviceStatus.Alive // Alive status for second try for second test
                ),
                clearPackageResults = listOf(
                    succeedClearPackage(), // Clear test package for second try for first test
                    succeedClearPackage(), // Clear application package for second try for first test

                    succeedClearPackage(), // Clear test package for first try for second test
                    succeedClearPackage(), // Clear application package for first try for second test

                    succeedClearPackage(), // Clear test package for second try for second test
                    succeedClearPackage() // Clear application package for second try for second test
                ),
                runTestsResults = listOf(
                    testPassed(), // First test passed by first try
                    testFailed(), // First test failed by second try
                    testPassed(), // First test passed by second try
                    testFailed() // Second test failed by second try
                )
            )

            devices.send(device)

            val result = runner.runTests(tests)
            device.verify()

            assertRunTestResult(
                runnerResult = result.getOrThrow(),
                expected = tests.map { test ->
                    ExpectedTestResult(
                        testCase = test,
                        deviceTestCaseRuns = listOf(
                            test.toPassedRun(device),
                            test.toFailedRun(device)
                        )
                    )
                }
            )
        }

    @Test
    fun `devices channel closed - run failed`() {
        val exception = assertThrows<IllegalStateException> {
            runBlockingTest {
                devices.close()
                val runner = provideRunner(targets = listOf(createTarget()))
                val tests = createTests(count = 2)
                runner.runTests(tests)
            }
        }

        assertThat(exception.message)
            .isEqualTo("devices channel was closed")
    }

    private fun createTarget(quota: QuotaConfigurationData = defaultQuota) =
        TargetConfigurationData.createStubInstance(
            api = deviceApi,
            model = deviceModel,
            deviceName = deviceName,
            quota = quota
        )

    private class ExpectedTestResult(
        val testCase: TestCase,
        val deviceTestCaseRuns: List<DeviceTestCaseRun>
    )

    private fun assertRunTestResult(
        runnerResult: TestRunnerResult,
        expected: List<ExpectedTestResult>
    ) {
        val actual = runnerResult.runs.toList()
        assertThat(actual).hasSize(expected.size)
        actual.forEachIndexed { testIndex, (testCase, runs) ->
            val expectedTestResult = expected[testIndex]
            assertThat(testCase).isEqualTo(expectedTestResult.testCase)
            assertThat(runs).hasSize(expectedTestResult.deviceTestCaseRuns.size)
            runs.forEachIndexed { runIndex, run ->
                assertThat(run).isEqualTo(expectedTestResult.deviceTestCaseRuns[runIndex])
            }
        }
    }

    private fun createBrokenDevice(failureReason: Exception): StubDevice {
        return StubDevice(
            tag = "StubDevice:broken",
            model = deviceModel,
            apiResult = StubActionResult.Success(deviceApi),
            loggerFactory = loggerFactory,
            coordinate = DeviceCoordinate.Local.createStubInstance(),
            gettingDeviceStatusResults = listOf(
                DeviceStatus.Freeze(failureReason)
            )
        )
    }

    private fun createTests(count: Int): List<TestCase> {
        return (1..count).map { index -> TestCase(TestName("Test", "test_$index"), deviceName) }
    }

    private fun TestCase.toPassedRun(
        device: StubDevice
    ): DeviceTestCaseRun {
        return deviceTestCaseRun(
            device = device,
            test = this,
            result = TestCaseRun.Result.Passed
        )
    }

    private fun TestCase.toFailedRun(
        device: StubDevice
    ): DeviceTestCaseRun {
        return deviceTestCaseRun(
            device = device,
            test = this,
            result = TestCaseRun.Result.Failed.InRun("Failed")
        )
    }

    private fun deviceIsFreezed(): DeviceStatus {
        return DeviceStatus.Freeze(reason = Exception())
    }

    private fun testFailed(): StubActionResult.Success<TestCaseRun.Result> {
        return StubActionResult.Success(
            TestCaseRun.Result.Failed.InRun("Failed")
        )
    }

    private fun createSuccessfulDevice(testsCount: Int): StubDevice {
        return StubDevice(
            tag = "StubDevice:normal",
            model = deviceModel,
            apiResult = StubActionResult.Success(deviceApi),
            loggerFactory = loggerFactory,
            coordinate = DeviceCoordinate.Local.createStubInstance(),
            installApplicationResults = mutableListOf(
                installApplicationSuccess(), // Install application
                installApplicationSuccess() // Install test application
            ),
            clearPackageResults = (0 until testsCount - 1).flatMap {
                listOf(
                    succeedClearPackage(),
                    succeedClearPackage()
                )
            },
            gettingDeviceStatusResults = List(testsCount + 1) { DeviceStatus.Alive },
            runTestsResults = (0 until testsCount).map {
                testPassed()
            }
        )
    }

    private fun testPassed(): StubActionResult.Success<TestCaseRun.Result> {
        return StubActionResult.Success(
            TestCaseRun.Result.Passed
        )
    }

    private fun succeedClearPackage() = StubActionResult.Success<Result<Unit>>(Result.Success(Unit))

    private fun provideRunner(
        targets: List<TargetConfigurationData>
    ): TestRunner {
        val testRunRequestFactory = TestRunRequestFactory(
            application = File("stub"),
            testApplication = File("stub"),
            executionParameters = ExecutionParameters.Companion.createStubInstance(),
            targets = targets.associateBy { it.deviceName }
        )
        val scheduler = TestExecutionScheduler(
            dispatcher = testCoroutineDispatcher,
            results = state.results,
            intentions = state.intentions,
            intentionResults = state.intentionResults
        )

        return TestRunnerImpl(
            scheduler = scheduler,
            loggerFactory = loggerFactory,
            state = state,
            reservationWatcher = object : DeviceReservationWatcher {
                override suspend fun watch(deviceSignals: ReceiveChannel<Signal>) {
                    // empty
                }
            },
            summaryReportMaker = SummaryReportMakerImpl(),
            reporter = CompositeReporter(emptyList()),
            traceReporter = StubTraceReporter(),
            testSuiteListener = StubTestMetricsListener,
            devicesProvider = StubDevicesProvider(
                provider = DeviceWorkerPoolProvider(
                    timeProvider = StubTimeProvider(),
                    loggerFactory = loggerFactory,
                    deviceListener = StubDeviceListener(),
                    newDeviceListener = com.avito.runner.listener.StubDeviceListener(),
                    intentions = state.intentions,
                    intentionResults = state.intentionResults,
                    deviceSignals = state.deviceSignals,
                    dispatchers = object : Dispatchers {
                        override fun dispatcher() = testCoroutineDispatcher
                    },
                    testRunnerOutputDir = outputDirectory,
                    testListener = NoOpTestListener
                ),
                devices = devices
            ),
            testRunRequestFactory = testRunRequestFactory,
            targets = targets,
        )
    }

    private fun deviceTestCaseRun(
        device: Device,
        test: TestCase,
        result: TestCaseRun.Result
    ): DeviceTestCaseRun = DeviceTestCaseRun(
        testCaseRun = TestCaseRun(
            test = test,
            result = result,
            timestampStartedMilliseconds = 0,
            timestampCompletedMilliseconds = 0
        ),
        device = device.getData()
    )

    private fun runBlockingTest(block: suspend TestCoroutineScope.() -> Unit) {
        testCoroutineDispatcher.runBlockingTest(block)
    }
}
