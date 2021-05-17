package com.avito.instrumentation.service

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

@Suppress("UnstableApiUsage")
public abstract class TestRunnerWorkAction : WorkAction<TestRunnerWorkAction.Params> {

    internal interface Params : WorkParameters {

        val service: Property<TestRunnerService>

        val statsDConfig: Property<StatsDConfig>

        val testRunParams: Property<TestRunParams>

        // todo replace with testRunParams completely ; knows too much
        val legacyTestRunParams: Property<InstrumentationTestsAction.Params>
    }

    override fun execute() {

        val params = parameters.testRunParams.get()

        val metricsConfig = RunnerMetricsConfig(
            statsDConfig = parameters.statsDConfig.get(),
            runnerPrefix = params.metricsPrefix
        )

        val legacyTestRunParams = parameters.legacyTestRunParams.get()

        val factory = createTestsActionFactory(legacyTestRunParams, metricsConfig)

        val testResults = parameters.service.get().runTests(params, legacyTestRunParams)

        factory.provideFinalizer().finalize(testResults)
    }

    private fun createTestsActionFactory(
        params: InstrumentationTestsAction.Params,
        metricsConfig: RunnerMetricsConfig
    ): InstrumentationTestsActionFactory {
        return InstrumentationTestsActionFactory.Impl(params, metricsConfig)
    }
}
