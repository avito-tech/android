package com.avito.runner.scheduler.suite.filter

import com.avito.android.Result
import com.avito.report.StubReport
import com.avito.report.model.TestStatus
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.config.InstrumentationFilterData.FromRunHistory.ReportFilter
import com.avito.runner.config.RunStatus
import com.avito.runner.config.createStub
import com.avito.runner.scheduler.suite.filter.TestsFilter.Signatures.Source
import com.avito.runner.scheduler.suite.filter.TestsFilter.Signatures.TestSignature
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.test.model.TestName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class FilterFactoryImplTest {

    @Test
    fun `when filterData is empty then filters always contains ExcludedBySdk and ExcludeAnnotationFilter`() {
        val factory = StubFilterFactoryFactory.create()

        val filter = factory.createFilter() as CompositionFilter

        assertThat(filter.filters)
            .containsExactly(
                ExcludeBySkipOnSdkFilter(),
                ExcludeAnnotationsFilter(setOf(FilterFactory.JUNIT_IGNORE_ANNOTATION))
            )
    }

    @Test
    fun `when filterData contains included annotations then filters have IncludeAnnotationFilter`() {
        val annotation = "Annotation"
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
                annotations = Filter.Value(
                    included = setOf(annotation),
                    excluded = emptySet()
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        assertThat(filter.filters)
            .containsAtLeastElementsIn(listOf(IncludeAnnotationsFilter(setOf(annotation))))
    }

    @Test
    fun `when filterData contains prefixes then filters have IncludeBySignatures, ExcludeBySignatures`() {
        val includedPrefix = "included_prefix"
        val excludedPrefix = "excluded_prefix"
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
                prefixes = Filter.Value(
                    included = setOf(includedPrefix),
                    excluded = setOf(excludedPrefix)
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        assertThat(filter.filters)
            .containsAtLeastElementsIn(
                listOf(
                    IncludeByTestSignaturesFilter(
                        source = Source.Code,
                        signatures = setOf(
                            TestSignature(
                                name = includedPrefix
                            )
                        )
                    ),
                    ExcludeByTestSignaturesFilter(
                        source = Source.Code,
                        signatures = setOf(
                            TestSignature(
                                name = excludedPrefix
                            )
                        )
                    )
                )
            )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `when filterData includePrevious statuses and Report returns list without that status then filters contain IncludeTestSignaturesFilters#Previous with empty signatures`() {
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
                previousStatuses = Filter.Value(
                    included = setOf(RunStatus.Failed),
                    excluded = emptySet()
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        val that = assertThat(filter.filters)
        that.containsAtLeastElementsIn(
            listOf(
                IncludeByTestSignaturesFilter(
                    source = Source.PreviousRun,
                    signatures = emptySet()
                )
            )
        )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `when filterData - includePrevious statuses and Report failed - then filters contain defaults`() {
        val report = StubReport()
        report.previousRunResults = Result.Failure(IllegalStateException("something went wrong"))

        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
                previousStatuses = Filter.Value(
                    included = setOf(RunStatus.Success),
                    excluded = emptySet()
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        val that = assertThat(filter.filters)

        that.containsAtLeastElementsIn(
            listOf(
                ExcludeBySkipOnSdkFilter(),
                ExcludeAnnotationsFilter(setOf(FilterFactory.JUNIT_IGNORE_ANNOTATION))
            )
        )
    }

    @Test
    fun `when filterData previousStatuses is empty then filters don't contain PreviousRun filters`() {
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
                previousStatuses = Filter.Value(
                    included = emptySet(),
                    excluded = emptySet()
                )
            )
        )

        val compositionFilter = factory.createFilter() as CompositionFilter

        compositionFilter.filters.forEach { filter ->
            assertThat(filter).run {
                isNotInstanceOf(IncludeByTestSignaturesFilter::class.java)
                isNotInstanceOf(ExcludeByTestSignaturesFilter::class.java)
            }
        }
    }

    @Test
    fun `when filterData report is empty then filters don't contain Report filters`() {
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub()
        )

        val compositionFilter = factory.createFilter() as CompositionFilter

        compositionFilter.filters.forEach { filter ->
            assertThat(filter).run {
                isNotInstanceOf(IncludeByTestSignaturesFilter::class.java)
                isNotInstanceOf(ExcludeByTestSignaturesFilter::class.java)
            }
        }
    }

    @Test
    fun `when filterData report is present and statuses empty then filters don't contain Report filter`() {
        val report = StubReport()
        val reportId = "report#1"
        report.reportIdToRunResults = Result.Success(
            mapOf(
                reportId to mapOf(
                    TestCase(TestName("", "test1"), DeviceName("25")) to TestStatus.Success,
                    TestCase(TestName("", "test2"), DeviceName("25")) to TestStatus.Lost
                )
            )
        )

        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
                report = ReportFilter(
                    reportId = reportId,
                    statuses = Filter.Value(
                        included = emptySet(),
                        excluded = emptySet()
                    )
                )
            )
        )

        val compositionFilter = factory.createFilter() as CompositionFilter

        compositionFilter.filters.forEach { filter ->
            assertThat(filter).run {
                isNotInstanceOf(IncludeByTestSignaturesFilter::class.java)
                isNotInstanceOf(ExcludeByTestSignaturesFilter::class.java)
            }
        }
    }

    @Test
    fun `when report for required id is not present then filters don't contain Report filter`() {
        val report = StubReport()
        report.reportIdToRunResults = Result.Success(
            mapOf(
                "report#1" to mapOf(
                    TestCase(TestName("", "test1"), DeviceName("25")) to TestStatus.Success,
                    TestCase(TestName("", "test2"), DeviceName("25")) to TestStatus.Lost
                )
            )
        )

        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
                report = ReportFilter(
                    reportId = "report#2",
                    statuses = Filter.Value(
                        included = setOf(RunStatus.Success),
                        excluded = setOf(RunStatus.Lost)
                    )
                )
            )
        )

        val compositionFilter = factory.createFilter() as CompositionFilter

        compositionFilter.filters.forEach { filter ->
            assertThat(filter).run {
                isNotInstanceOf(IncludeByTestSignaturesFilter::class.java)
                isNotInstanceOf(ExcludeByTestSignaturesFilter::class.java)
            }
        }
    }
}
