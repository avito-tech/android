package com.avito.instrumentation

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus
import com.avito.instrumentation.configuration.InstrumentationParameters
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.suite.filter.Filter

fun InstrumentationConfiguration.Data.Companion.createStubInstance(
    name: String = "name",
    performanceType: InstrumentationConfiguration.PerformanceType? = null,
    instrumentationParams: InstrumentationParameters = InstrumentationParameters(),
    tryToReRunOnTargetBranch: Boolean = false,
    rerunFailedTests: Boolean = false,
    reportSkippedTests: Boolean = true,
    reportFlakyTests: Boolean = false,
    prefixFilter: String? = null,
    annotatedWith: List<String>? = null,
    impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off,
    tests: List<String>? = null,
    kubernetesNamespace: String = "kubernetesNamespace",
    targets: List<TargetConfiguration.Data> = emptyList(),
    enableDeviceDebug: Boolean = false,
    previousRunExcluded: Set<RunStatus> = emptySet()
): InstrumentationConfiguration.Data = InstrumentationConfiguration.Data(
    name = name,
    performanceType = performanceType,
    instrumentationParams = instrumentationParams,
    keepTestsWithPrefix = prefixFilter,
    tryToReRunOnTargetBranch = tryToReRunOnTargetBranch,
    skipSucceedTestsFromPreviousRun = rerunFailedTests,
    reportFlakyTests = reportFlakyTests,
    reportSkippedTests = reportSkippedTests,
    keepTestsAnnotatedWith = annotatedWith,
    impactAnalysisPolicy = impactAnalysisPolicy,
    keepTestsWithNames = tests,
    kubernetesNamespace = kubernetesNamespace,
    targets = targets,
    enableDeviceDebug = enableDeviceDebug,
    keepFailedTestsFromReport = null,
    filter = InstrumentationFilter.Data(
        name = "stub",
        fromSource = InstrumentationFilter.Data.FromSource(
            prefixes = Filter.Value(
                included = emptySet(),
                excluded = emptySet()
            ),
            annotations = Filter.Value(
                included = emptySet(),
                excluded = emptySet()
            )
        ),
        fromRunHistory = InstrumentationFilter.Data.FromRunHistory(
            previousStatuses = Filter.Value(
                included = emptySet(),
                excluded = previousRunExcluded
            ),
            reportFilter = null
        )
    )
)
