package com.avito.runner.scheduler.runner

import com.avito.android.Result
import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.model.ReservationData
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.metrics.TestSuiteListener
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.model.TestRunnerResult
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.trace.TraceReporter
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.time.millisecondsToHumanReadableTime
import kotlinx.coroutines.coroutineScope

internal class TestRunnerImpl(
    private val scheduler: TestExecutionScheduler,
    private val reservationWatcher: DeviceReservationWatcher,
    private val state: TestRunnerExecutionState,
    private val summaryReportMaker: SummaryReportMaker,
    private val reporter: Reporter,
    private val traceReporter: TraceReporter,
    private val testSuiteListener: TestSuiteListener,
    private val devicesProvider: DevicesProvider,
    private val testRunRequestFactory: TestRunRequestFactory,
    private val targets: List<TargetConfigurationData>,
    loggerFactory: LoggerFactory
) : TestRunner {

    private val logger = loggerFactory.create<TestRunner>()

    override suspend fun runTests(tests: List<TestCase>): Result<TestRunnerResult> {
        return coroutineScope {
            val startTime = System.currentTimeMillis()
            testSuiteListener.onTestSuiteStarted()
            logger.info("Test run started")
            val deviceWorkerPool: DeviceWorkerPool = devicesProvider.provideFor(
                reservations = getReservations(tests),
            )
            try {
                deviceWorkerPool.start()
                reservationWatcher.watch(state.deviceSignals)
                scheduler.start(
                    requests = getTestRequests(tests),
                )

                val expectedResultsCount = tests.count()

                val gottenResults = mutableListOf<TestRunResult>()
                for (result in state.results) {
                    gottenResults.add(result)
                    val gottenCount = gottenResults.size

                    logger.debug(
                        "Result for test: %s received after %d tries. Progress (%s)".format(
                            result.request.testCase.name,
                            result.result.size,
                            "$gottenCount/$expectedResultsCount"
                        )
                    )

                    if (gottenCount == expectedResultsCount) {
                        break
                    }
                }
                val result = TestRunnerResult(
                    runs = gottenResults.associate {
                        it.request.testCase to it.result
                    }
                )

                val summaryReport = summaryReportMaker.make(gottenResults, startTime)
                reporter.report(report = summaryReport)
                logger.debug(
                    "Test run finished. The results: " +
                        "passed = ${summaryReport.successRunsCount}, " +
                        "failed = ${summaryReport.failedRunsCount}, " +
                        "ignored = ${summaryReport.ignoredRunsCount}, " +
                        "took ${summaryReport.durationMilliseconds.millisecondsToHumanReadableTime()}."
                )
                logger.debug(
                    "Matching results: " +
                        "matched = ${summaryReport.matchedCount}, " +
                        "mismatched = ${summaryReport.mismatched}, " +
                        "ignored = ${summaryReport.ignoredCount}."
                )

                testSuiteListener.onTestSuiteFinished()
                logger.info("Test run end successfully")
                Result.Success(result)
            } catch (e: Throwable) {
                logger.critical("Test run end with error", e)
                Result.Failure(e)
            } finally {
                traceReporter.report()
                deviceWorkerPool.stop()
                state.cancel()
                devicesProvider.releaseDevices()
            }
        }
    }

    private fun getReservations(tests: List<TestCase>): Collection<ReservationData> {
        val testsGroupedByDevice: Map<DeviceName, List<TestCase>> = tests.groupBy { test ->
            test.deviceName
        }

        return testsGroupedByDevice
            .map { (deviceName, tests) ->
                val target = requireNotNull(targets.firstOrNull { it.deviceName == deviceName }) {
                    "Can't find target $deviceName"
                }
                target.reservation.data(tests.size)
            }
    }

    private fun getTestRequests(tests: List<TestCase>) =
        tests.map { test -> testRunRequestFactory.create(test) }
}
