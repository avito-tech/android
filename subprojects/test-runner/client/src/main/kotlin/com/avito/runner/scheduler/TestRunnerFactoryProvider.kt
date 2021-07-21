package com.avito.runner.scheduler

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.report.Report
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.listener.TestListenerFactory
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender
import com.avito.runner.scheduler.metrics.TestMetricsListenerImpl
import com.avito.runner.scheduler.metrics.TestMetricsSender
import com.avito.runner.scheduler.runner.TestRunnerExecutionState
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.runner.trace.TraceReporter
import com.avito.runner.trace.TraceReporterFactory
import com.avito.time.TimeProvider
import java.io.File
import java.nio.file.Files

public class TestRunnerFactoryProvider(
    private val params: RunnerInputParams,
    private val timeProvider: TimeProvider,
    private val httpClientProvider: HttpClientProvider,
    private val report: Report,
    private val devicesProviderFactory: DevicesProviderFactory,
    metricsConfig: RunnerMetricsConfig,
) {

    private val loggerFactory = params.loggerFactory
    private val outputDir = params.outputDir
    private val tempLogcatDir = Files.createTempDirectory(null).toFile()
    private val testRunnerExecutionState = TestRunnerExecutionState()

    private val statsDSender: StatsDSender = StatsDSender.create(
        config = metricsConfig.statsDConfig,
        loggerFactory = loggerFactory
    )

    private val metricsSender = InstrumentationMetricsSender(
        statsDSender = statsDSender,
        runnerPrefix = metricsConfig.runnerPrefix
    )

    private val testMetricsSender = TestMetricsListenerImpl(
        testMetricsSender = TestMetricsSender(
            statsDSender = statsDSender,
            prefix = metricsConfig.runnerPrefix
        ),
        timeProvider = timeProvider,
        loggerFactory = loggerFactory
    )

    /**
     * i.e {projectDir}/output/test-runner/
     */
    private val testRunnerOutputDir: File by lazy {
        File(
            outputDir,
            "test-runner"
        ).apply { mkdirs() }
    }

    private val traceReporter: TraceReporter = TraceReporterFactory.create(
        timeProvider = timeProvider,
        outputDirectory = testRunnerOutputDir
    )

    internal fun provide(): TestRunnerFactory {
        return TestRunnerFactoryImpl(
            testRunnerOutputDir = testRunnerOutputDir,
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
            testSuiteListener = testMetricsSender,
            deviceListener = testMetricsSender,
            traceReporter = traceReporter,
            devicesProviderFactory = devicesProviderFactory,
            testRunnerRequestFactory = testRunRequestFactory(),
            executionState = testRunnerExecutionState,
            params = params,
            tempLogcatDir = tempLogcatDir,
            testListenerFactory = TestListenerFactory(
                loggerFactory = loggerFactory,
                timeProvider = timeProvider,
                httpClientProvider = httpClientProvider
            ),
            report = report,
            targets = params.instrumentationConfiguration.targets,
            metricsSender = metricsSender,
        )
    }

    private fun testRunRequestFactory(): TestRunRequestFactory {
        return TestRunRequestFactory(
            application = params.mainApk,
            testApplication = params.testApk,
            executionParameters = params.executionParameters,
            targets = params.instrumentationConfiguration.targets.associateBy { it.deviceName }
        )
    }
}
