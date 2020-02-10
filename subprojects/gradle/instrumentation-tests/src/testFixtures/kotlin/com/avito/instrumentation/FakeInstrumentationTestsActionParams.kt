package com.avito.instrumentation

import com.avito.android.stats.StatsDConfig
import com.avito.bitbucket.AtlassianCredentials
import com.avito.bitbucket.BitbucketConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import com.avito.test.gradle.randomCommitHash
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.logging.CILogger
import java.io.File

fun InstrumentationTestsAction.Params.Companion.createStubInstance(
    mainApk: File = File(""),
    testApk: File = File(""),
    apkOnTargetCommit: File = File(""),
    testApkOnTargetCommit: File = File(""),
    instrumentationConfiguration: InstrumentationConfiguration.Data = InstrumentationConfiguration.Data.createStubInstance(),
    executionParameters: ExecutionParameters = ExecutionParameters.createStubInstance(),
    buildId: String = "33456",
    buildUrl: String = "https://build",
    targetCommit: String = randomCommitHash(),
    kubernetesCredentials: KubernetesCredentials = object :
        KubernetesCredentials {
        override val token: String
            get() = error("not implemented")
        override val caCertData: String
            get() = error("not implemented")
        override val url: String
            get() = error("not implemented")
    },
    projectName: String = "testProject",
    testedVariantName: String = "debug",
    suppressFailure: Boolean = false,
    impactAnalysisResult: File? = null,
    logger: CILogger = CILogger.allToStdout,
    sendStatistics: Boolean = false,
    slackToken: String = "slack",
    sourceCommitHash: String = "",
    currentBranch: String = "develop",
    outputDir: File = File("."),
    downsamplingFactor: Float? = null,
    reportId: String? = null,
    reportApiUrl: String = "https://reports",
    reportApiFallbackUrl: String = "https://reports",
    reportViewerUrl: String = "https://reports",
    registry: String = "",
    fileStorageUrl: String = "https://files",
    pullRequestId: Int? = null,
    isFullTestSuite: Boolean = false,
    bitbucketConfig: BitbucketConfig = BitbucketConfig(
        baseUrl = "http://bitbucket",
        credentials = AtlassianCredentials("xxx", "xxx"),
        projectKey = "AA",
        repositorySlug = "android"
    ),
    statsDConfig: StatsDConfig = StatsDConfig(false, "", "", 0, ""),
    unitToChannelMapping: Map<Team, SlackChannel> = emptyMap()
) =
    InstrumentationTestsAction.Params(
        mainApk = mainApk,
        testApk = testApk,
        apkOnTargetCommit = apkOnTargetCommit,
        testApkOnTargetCommit = testApkOnTargetCommit,
        instrumentationConfiguration = instrumentationConfiguration,
        executionParameters = executionParameters,
        buildId = buildId,
        buildUrl = buildUrl,
        targetCommit = targetCommit,
        kubernetesCredentials = kubernetesCredentials,
        projectName = projectName,
        testedVariantName = testedVariantName,
        suppressFailure = suppressFailure,
        impactAnalysisResult = impactAnalysisResult,
        logger = logger,
        currentBranch = currentBranch,
        sourceCommitHash = sourceCommitHash,
        outputDir = outputDir,
        sendStatistics = sendStatistics,
        slackToken = slackToken,
        downsamplingFactor = downsamplingFactor,
        reportId = reportId,
        reportApiUrl = reportApiUrl,
        fileStorageUrl = fileStorageUrl,
        pullRequestId = pullRequestId,
        isFullTestSuite = isFullTestSuite,
        bitbucketConfig = bitbucketConfig,
        statsdConfig = statsDConfig,
        unitToChannelMapping = unitToChannelMapping,
        reportViewerUrl = reportViewerUrl,
        reportApiFallbackUrl = reportApiFallbackUrl,
        registry = registry
    )
