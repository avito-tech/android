package com.avito.runner.config

import com.avito.android.stats.StatsDConfig
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisResult
import com.avito.utils.gradle.KubernetesCredentials
import java.io.File
import java.io.Serializable
import java.time.Duration

public data class RunnerInputParams(
    val mainApk: File,
    val testApk: File,
    val instrumentationConfiguration: InstrumentationConfigurationData,
    val executionParameters: ExecutionParameters,
    val buildId: String,
    val buildType: String,
    val kubernetesCredentials: KubernetesCredentials,
    val kubernetesHttpTries: Int,
    val deviceDebug: Boolean,
    val projectName: String,
    val suppressFailure: Boolean,
    val suppressFlaky: Boolean,
    val impactAnalysisResult: ImpactAnalysisResult,
    val outputDir: File,
    val macrobenchmarkOutputDir: File?,
    val verdictFile: File,
    val statsDConfig: StatsDConfig,
    val proguardMappings: List<File>,
    val saveTestArtifactsToOutputs: Boolean,
    val useLegacyExtensionsV1Beta: Boolean,
    val disableLogcat: Boolean,
    val adbPullTimeout: Duration,
) : Serializable {

    public companion object
}
