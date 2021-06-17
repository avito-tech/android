package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.Result
import com.avito.android.TestInApk
import com.avito.android.TestSuiteLoader
import com.avito.android.check.AllChecks
import com.avito.android.runner.report.Report
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.scheduler.TestRunnerFactory
import com.avito.runner.scheduler.runner.model.TestSchedulerResult
import com.avito.runner.scheduler.suite.TestSuite
import com.avito.runner.scheduler.suite.TestSuiteProvider
import com.avito.runner.scheduler.suite.filter.FilterInfoWriter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import java.io.File

internal class TestSchedulerImpl(
    private val params: InstrumentationTestsActionParams,
    private val report: Report,
    private val testSuiteProvider: TestSuiteProvider,
    private val testSuiteLoader: TestSuiteLoader,
    private val filterInfoWriter: FilterInfoWriter,
    private val testRunnerFactory: TestRunnerFactory,
    loggerFactory: LoggerFactory,
) : TestScheduler {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val logger = loggerFactory.create<TestSchedulerImpl>()

    override fun schedule(): TestSchedulerResult {
        logger.debug("Filter config: ${params.instrumentationConfiguration.filter}")
        filterInfoWriter.writeFilterConfig(params.instrumentationConfiguration.filter)

        val tests = testSuiteLoader.loadTestSuite(params.testApk, AllChecks())

        tests.fold(
            { result ->
                logger.info("Tests parsed from apk: ${result.size}")
                logger.debug("Tests parsed from apk: ${result.map { it.testName }}")
            },
            { error -> logger.critical("Can't parse tests from apk", error) }
        )

        writeParsedTests(tests)

        val testSuite = testSuiteProvider.getTestSuite(
            tests = tests.getOrThrow()
        )

        val skippedTests = testSuite.skippedTests.map {
            "${it.first.test.name} on ${it.first.target.deviceName} because ${it.second.reason}"
        }
        logger.debug("Skipped tests: $skippedTests")

        val testsToRun = testSuite.testsToRun
        logger.debug("Tests to run: ${testsToRun.map { "${it.test.name} on ${it.target.deviceName}" }}")

        filterInfoWriter.writeAppliedFilter(testSuite.appliedFilter)
        filterInfoWriter.writeFilterExcludes(testSuite.skippedTests)

        writeTestSuite(testSuite)

        if (testsToRun.isNotEmpty()) {
            runBlocking {
                testRunnerFactory.createTestRunner().runTests(testsToRun)
            }
        }

        return TestSchedulerResult(
            testsToRun = testSuite.testsToRun.map { it.test },
            testResults = report.getTestResults()
        )
    }

    private fun writeParsedTests(parsedTests: Result<List<TestInApk>>) {
        val file = File(params.outputDir, "parsed-tests.json")
        parsedTests.fold(
            { tests -> file.writeText(gson.toJson(tests)) },
            { t -> file.writeText("There was an error while parsing tests:\n $t") }
        )
    }

    private fun writeTestSuite(testSuite: TestSuite) {
        File(params.outputDir, "test-suite.json")
            .writeText(gson.toJson(testSuite.testsToRun.map { it.test }))
    }
}
