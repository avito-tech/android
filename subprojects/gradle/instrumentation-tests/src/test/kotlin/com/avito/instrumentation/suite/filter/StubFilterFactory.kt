package com.avito.instrumentation.suite.filter

import com.avito.android.runner.report.ReportFactory
import com.avito.android.runner.report.StubReportFactory
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.createStub
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory

internal object StubFilterFactory {

    fun create(
        filter: InstrumentationFilter.Data = InstrumentationFilter.Data.createStub(),
        impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
        loggerFactory: LoggerFactory = StubLoggerFactory,
        reportFactory: ReportFactory = StubReportFactory()
    ): FilterFactory {
        return FilterFactory.create(
            filterData = filter,
            impactAnalysisResult = impactAnalysisResult,
            reportFactory = reportFactory,
            loggerFactory = loggerFactory
        )
    }
}
