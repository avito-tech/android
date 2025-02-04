package com.avito.runner.config

import com.avito.android.stats.StatsDConfig
import com.avito.runner.scheduler.runner.createStubInstance
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisResult
import com.avito.runner.scheduler.suite.filter.createStubInstance
import com.avito.utils.gradle.KubernetesCredentials
import java.io.File
import java.time.Duration
import kotlin.io.path.createTempDirectory

public fun RunnerInputParams.Companion.createStubInstance(
    mainApk: File = File(""),
    testApk: File = File(""),
    instrumentationConfiguration: InstrumentationConfigurationData =
        InstrumentationConfigurationData.createStubInstance(),
    executionParameters: ExecutionParameters = ExecutionParameters.createStubInstance(),
    buildId: String = "33456",
    buildType: String = "teamcity",
    kubernetesCredentials: KubernetesCredentials = KubernetesCredentials.Service(
        token = "empty",
        url = "empty",
        namespace = "kubernetesNamespace",
    ),
    kubernetesHttpTries: Int = 3,
    projectName: String = "testProject",
    suppressFailure: Boolean = false,
    suppressFlaky: Boolean = false,
    impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
    deviceDebug: Boolean = false,
    outputDir: File = createTempDirectory("runnerOutput").toFile(),
    macrobenchmarkOutputDir: File = createTempDirectory("runnerMacrobenchmarkOutput").toFile(),
    verdictFile: File = File(outputDir, "verdict.json"),
    statsDConfig: StatsDConfig = StatsDConfig.Disabled,
    saveTestArtifactsToOutputs: Boolean = false,
    useLegacyExtensionsV1Beta: Boolean = true,
    disableLogcat: Boolean = false,
): RunnerInputParams = RunnerInputParams(
    mainApk = mainApk,
    testApk = testApk,
    instrumentationConfiguration = instrumentationConfiguration,
    executionParameters = executionParameters,
    buildId = buildId,
    buildType = buildType,
    kubernetesCredentials = kubernetesCredentials,
    kubernetesHttpTries = kubernetesHttpTries,
    projectName = projectName,
    suppressFailure = suppressFailure,
    suppressFlaky = suppressFlaky,
    impactAnalysisResult = impactAnalysisResult,
    outputDir = outputDir,
    macrobenchmarkOutputDir = macrobenchmarkOutputDir,
    verdictFile = verdictFile,
    statsDConfig = statsDConfig,
    proguardMappings = emptyList(),
    saveTestArtifactsToOutputs = saveTestArtifactsToOutputs,
    useLegacyExtensionsV1Beta = useLegacyExtensionsV1Beta,
    deviceDebug = deviceDebug,
    adbPullTimeout = Duration.ofSeconds(5),
    disableLogcat = disableLogcat,
)
