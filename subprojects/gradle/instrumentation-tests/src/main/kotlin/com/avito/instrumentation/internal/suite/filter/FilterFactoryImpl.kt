package com.avito.instrumentation.internal.suite.filter

import com.avito.android.runner.report.factory.ReportFactory
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory
import com.avito.instrumentation.internal.suite.filter.FilterFactory.Companion.JUNIT_IGNORE_ANNOTATION
import com.avito.instrumentation.internal.suite.filter.TestsFilter.Signatures.TestSignature
import com.avito.report.model.SimpleRunTest

internal class FilterFactoryImpl(
    private val filterData: InstrumentationFilter.Data,
    private val impactAnalysisResult: ImpactAnalysisResult,
    private val factory: ReportFactory,
    private val reportConfig: ReportFactory.Config
) : FilterFactory {

    override fun createFilter(): TestsFilter {
        val filters = mutableListOf<TestsFilter>()
        filters.add(ExcludeBySkipOnSdkFilter())
        filters.addFlakyFilter()
        filters.addAnnotationFilters()
        filters.addSourceCodeSignaturesFilters()
        filters.addSourcePreviousSignatureFilters()
        filters.addSourceReportSignatureFilters()
        filters.addImpactAnalysisFilter()
        return CompositionFilter(filters)
    }

    private fun MutableList<TestsFilter>.addFlakyFilter() {
        if (filterData.fromSource.excludeFlaky) {
            add(ExcludeByFlakyFilter())
        }
    }

    private fun MutableList<TestsFilter>.addAnnotationFilters() {
        if (filterData.fromSource.annotations.included.isNotEmpty()) {
            add(
                IncludeAnnotationsFilter(
                    filterData.fromSource.annotations.included
                )
            )
        }
        add(
            ExcludeAnnotationsFilter(
                filterData.fromSource.annotations.excluded + JUNIT_IGNORE_ANNOTATION
            )
        )
    }

    private fun MutableList<TestsFilter>.addSourceCodeSignaturesFilters() {
        val prefixes = filterData.fromSource.prefixes
        if (prefixes.included.isNotEmpty()) {
            add(
                IncludeByTestSignaturesFilter(
                    source = TestsFilter.Signatures.Source.Code,
                    signatures = prefixes.included
                        .map {
                            TestSignature(
                                name = it
                            )
                        }.toSet()
                )
            )
        }
        if (prefixes.excluded.isNotEmpty()) {
            add(
                ExcludeByTestSignaturesFilter(
                    source = TestsFilter.Signatures.Source.Code,
                    signatures = prefixes.excluded
                        .map {
                            TestSignature(
                                name = it
                            )
                        }.toSet()
                )
            )
        }
    }

    private fun MutableList<TestsFilter>.addSourcePreviousSignatureFilters() {
        val previousStatuses = filterData.fromRunHistory.previousStatuses
        if (previousStatuses.included.isNotEmpty() || previousStatuses.excluded.isNotEmpty()) {
            val previousRunTests = factory
                .createReadReport(reportConfig)
                .getTests()
                .getOrThrow()
            if (previousStatuses.included.isNotEmpty()) {
                add(
                    IncludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.PreviousRun,
                        signatures = previousRunTests.filterBy(previousStatuses.included)
                    )
                )
            }
            if (previousStatuses.excluded.isNotEmpty()) {
                add(
                    ExcludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.PreviousRun,
                        signatures = previousRunTests.filterBy(previousStatuses.excluded)
                    )
                )
            }
        }
    }

    private fun MutableList<TestsFilter>.addSourceReportSignatureFilters() {
        val reportFilter = filterData.fromRunHistory.reportFilter
        if (reportFilter != null
            && (reportFilter.statuses.included.isNotEmpty()
                || reportFilter.statuses.excluded.isNotEmpty())
        ) {
            val statuses = reportFilter.statuses
            val previousRunTests = factory.createReadReport(reportFilter.reportConfig)
                .getTests()
                .getOrThrow()
            if (statuses.included.isNotEmpty()) {
                add(
                    IncludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.Report,
                        signatures = previousRunTests.filterBy(statuses.included)
                    )

                )
            }
            if (statuses.excluded.isNotEmpty()) {
                add(
                    ExcludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.Report,
                        signatures = previousRunTests.filterBy(statuses.excluded)
                    )

                )
            }
        }
    }

    private fun List<SimpleRunTest>.filterBy(statuses: Set<FromRunHistory.RunStatus>): Set<TestSignature> {
        return asSequence()
            .filter { testRun -> statuses.any { it.statusClass.isInstance(testRun.status) } }
            .map { testRun ->
                TestSignature(
                    name = testRun.name,
                    deviceName = testRun.deviceName
                )
            }.toSet()
    }

    private fun MutableList<TestsFilter>.addImpactAnalysisFilter() {
        if (impactAnalysisResult.runOnlyChangedTests) {
            addImpactTests(impactAnalysisResult.changedTests)
        }
    }

    private fun MutableList<TestsFilter>.addImpactTests(tests: List<String>) {
        add(
            IncludeByTestSignaturesFilter(
                source = TestsFilter.Signatures.Source.ImpactAnalysis,
                signatures = tests.map { name ->
                    TestSignature(
                        name = name
                    )
                }.toSet()
            )
        )
    }
}
