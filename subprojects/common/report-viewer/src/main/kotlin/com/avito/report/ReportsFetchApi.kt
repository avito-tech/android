package com.avito.report

import com.avito.android.Result
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest

public interface ReportsFetchApi {

    /**
     * Run.List
     */
    public fun getReportsList(planSlug: String, jobSlug: String, pageNumber: Int): Result<List<Report>>

    /**
     * Run.GetByParams
     */
    public fun getReport(reportCoordinates: ReportCoordinates): Result<Report>

    /**
     * RunTest.List
     * получение краткого списка результатов тестов по запуску
     */
    public fun getTestsForRunId(reportCoordinates: ReportCoordinates): Result<List<SimpleRunTest>>

    public fun getTestsForReportId(reportId: String): Result<List<SimpleRunTest>>

    public fun getCrossDeviceTestData(reportCoordinates: ReportCoordinates): Result<CrossDeviceSuite>
}
