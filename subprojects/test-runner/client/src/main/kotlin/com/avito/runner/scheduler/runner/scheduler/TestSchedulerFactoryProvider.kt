package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.TestSuiteLoader
import com.avito.android.runner.devices.DeviceProviderFactoryProvider
import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClientProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationListenerProvider
import com.avito.android.runner.devices.internal.kubernetes.ReservationDeploymentFactoryProvider
import com.avito.android.stats.StatsDSender
import com.avito.graphite.series.SeriesName
import com.avito.http.StatsDHttpEventListener
import com.avito.http.TagRequestMetadataProvider
import com.avito.k8s.K8SOkHttpClientFactory
import com.avito.k8s.KubernetesApiFactory
import com.avito.k8s.KubernetesClientFactory
import com.avito.logger.LoggerFactory
import com.avito.report.ReportViewerTestStaticDataParser
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.finalizer.FinalizerFactoryImpl
import com.avito.runner.scheduler.TestRunnerFactoryProvider
import com.avito.runner.scheduler.report.ReportModule
import com.avito.runner.scheduler.report.ReportModuleDependencies
import com.avito.runner.scheduler.suite.TestSuiteProvider
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.FilterInfoWriter
import com.avito.runner.scheduler.suite.filter.run_results_provider.RunResultsProviderImpl
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.ProcessRunner
import okhttp3.OkHttpClient
import java.io.File
import java.nio.file.Files

public class TestSchedulerFactoryProvider(private val loggerFactory: LoggerFactory) {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    public fun provide(params: RunnerInputParams): TestSchedulerFactory {
        val statsDSender = StatsDSender.create(
            config = params.statsDConfig,
            loggerFactory = loggerFactory
        )
        val httpClientBuilder = OkHttpClient.Builder()
            .eventListenerFactory {
                StatsDHttpEventListener(
                    statsDSender = statsDSender,
                    timeProvider = timeProvider,
                    requestMetadataProvider = TagRequestMetadataProvider(),
                    loggerFactory = loggerFactory,
                )
            }

        val metricsConfig = createRunnerMetricsConfig(params)

        /**
         * i.e {projectDir}/output/test-runner/
         */
        val testRunnerOutputDir: File =
            File(
                params.outputDir,
                "test-runner"
            ).apply { mkdirs() }

        val tempLogcatDir = Files.createTempDirectory(null).toFile()

        val reportModule = ReportModule.create(
            reportConfig = params.instrumentationConfiguration.reportConfig,
            dependencies = ReportModuleDependencies(
                timeProvider = timeProvider,
                loggerFactory = loggerFactory,
                httpClientBuilder = httpClientBuilder,
                testRunnerOutputDir = testRunnerOutputDir,
                tempLogcatDir = tempLogcatDir,
                params = params,
            )
        )

        val processRunner = ProcessRunner.create(null)
        val androidDebugBridgeProvider = AndroidDebugBridgeProvider(loggerFactory, processRunner)

        val emulatorsLogsReporterProvider = EmulatorsLogsReporterProvider(
            logcatTags = params.executionParameters.logcatTags,
            outputDir = params.outputDir,
            disableLogcat = params.disableLogcat,
        )

        return TestSchedulerFactoryImpl(
            finalizerFactory = FinalizerFactoryImpl(
                report = reportModule.report,
                metricsConfig = metricsConfig,
                timeProvider = timeProvider,
                loggerFactory = loggerFactory,
                reportConfig = params.instrumentationConfiguration.reportConfig,
                suppressFailure = params.suppressFailure,
                suppressFlaky = params.suppressFlaky,
                verdictFile = params.verdictFile,
                outputDir = params.outputDir,
            ),
            report = reportModule.report,
            testSuiteProvider = TestSuiteProvider.Impl(
                filterFactory = FilterFactory.create(
                    filterData = params.instrumentationConfiguration.filter,
                    impactAnalysisResult = params.impactAnalysisResult,
                    runResultsProvider = RunResultsProviderImpl(reportModule.report),
                    loggerFactory = loggerFactory
                ),
                testStaticParser = ReportViewerTestStaticDataParser.Impl(
                    targets = params.instrumentationConfiguration.targets.map {
                        ReportViewerTestStaticDataParser.TargetDevice(
                            it.deviceName,
                            it.reservation.device.api
                        )
                    }
                )
            ),
            testRunnerFactoryProvider = TestRunnerFactoryProvider(
                params = params,
                timeProvider = timeProvider,
                loggerFactory = loggerFactory,
                reportModule = reportModule,
                testRunnerOutputDir = testRunnerOutputDir,
                tempLogcatDir = tempLogcatDir,
                devicesProviderFactory = DeviceProviderFactoryProvider(
                    loggerFactory = loggerFactory,
                    timeProvider = timeProvider,
                    deviceType = params.instrumentationConfiguration.requestedDeviceType,
                    processRunner = processRunner,
                    kubernetesReservationClientProvider = KubernetesReservationClientProvider(
                        loggerFactory = loggerFactory,
                        kubernetesApiFactory = KubernetesApiFactory(
                            kubernetesClientFactory = KubernetesClientFactory(
                                kubernetesCredentials = params.kubernetesCredentials,
                                okHttpClientFactory = K8SOkHttpClientFactory(
                                    loggerFactory = loggerFactory,
                                    timeProvider = timeProvider,
                                    statsDSender = statsDSender,
                                    httpTries = params.kubernetesHttpTries,
                                ),
                            ),
                            loggerFactory = loggerFactory,
                        ),
                        androidDebugBridgeProvider = androidDebugBridgeProvider,
                        reservationDeploymentFactoryProvider = ReservationDeploymentFactoryProvider(
                            configurationName = params.instrumentationConfiguration.name,
                            projectName = params.projectName,
                            buildId = params.buildId,
                            buildType = params.buildType,
                            loggerFactory = loggerFactory,
                            useLegacyExtensionsV1Beta = params.useLegacyExtensionsV1Beta,
                        ),
                        emulatorsLogsReporterProvider = emulatorsLogsReporterProvider,
                        kubernetesReservationListenerProvider = KubernetesReservationListenerProvider(
                            timeProvider = timeProvider,
                            runnerMetricsConfig = metricsConfig,
                            loggerFactory = loggerFactory,
                        )
                    ),
                    androidDebugBridgeProvider = androidDebugBridgeProvider,
                    emulatorsLogsReporterProvider = emulatorsLogsReporterProvider,
                    metricsConfig = metricsConfig,
                    adbPullTimeout = params.adbPullTimeout
                ).provide(),
                metricsConfig = metricsConfig
            ),
            testSuiteLoader = TestSuiteLoader.create(),
            loggerFactory = loggerFactory,
            fileInfoWriter = FilterInfoWriter.Impl(
                outputDir = params.outputDir,
            ),
            filter = params.instrumentationConfiguration.filter,
            testApk = params.testApk,
            outputDir = params.outputDir,
            reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests,
        )
    }

    private fun createRunnerMetricsConfig(params: RunnerInputParams): RunnerMetricsConfig {
        return RunnerMetricsConfig(
            statsDConfig = params.statsDConfig,
            runnerPrefix = SeriesName.create(
                "testrunner",
                params.projectName,
                params.instrumentationConfiguration.name
            )
        )
    }
}
