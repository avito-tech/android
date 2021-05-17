package com.avito.instrumentation.service

import com.avito.android.runner.devices.DeviceProviderFactoryImpl
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

@Suppress("UnstableApiUsage")
public abstract class TestRunnerService : BuildService<TestRunnerService.Params>, AutoCloseable {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    private val devicesProviderFactory: DevicesProviderFactory = DeviceProviderFactoryImpl(
        kubernetesCredentials = parameters.kubernetesCredentials.get(),
        buildId = parameters.buildId.get(),
        buildType = parameters.buildType.get(),
        loggerFactory = parameters.loggerFactory.get(),
        timeProvider = timeProvider,
        statsDConfig = parameters.statsDConfig.get()
    )

    internal interface Params : BuildServiceParameters {

        val kubernetesCredentials: Property<KubernetesCredentials>

        val statsDConfig: Property<StatsDConfig>

        val buildId: Property<String>

        val buildType: Property<String>

        val loggerFactory: Property<LoggerFactory>
    }

    internal fun runTests(
        params: TestRunParams,
        legacyParams: InstrumentationTestsAction.Params
    ): TestsScheduler.Result {

        val metricsConfig = RunnerMetricsConfig(
            statsDConfig = parameters.statsDConfig.get(),
            runnerPrefix = params.metricsPrefix
        )

        val factory = createTestsActionFactory(legacyParams, metricsConfig)

        return factory.provideScheduler(devicesProviderFactory).schedule()
    }

    override fun close() {
    }

    private fun createTestsActionFactory(
        params: InstrumentationTestsAction.Params,
        metricsConfig: RunnerMetricsConfig
    ): InstrumentationTestsActionFactory {
        return InstrumentationTestsActionFactory.Impl(params, metricsConfig)
    }
}
