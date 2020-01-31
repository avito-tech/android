package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockReportsExtension::class)
internal class ExternalIdTest {

    @Test
    fun `externalId sent in prepared_data`(reports: MockReportApi) {
        val externalId = UUID.randomUUID().toString()
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(externalId = externalId)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.prepared_data.external_id", Matchers.equalTo(externalId)))
    }

    @Test
    fun `externalId contains dataSetNumber if present`(reports: MockReportApi) {
        val externalId = UUID.randomUUID().toString()
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    externalId = externalId,
                    dataSetNumber = 3
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.prepared_data.external_id", Matchers.equalTo("${externalId}_3")))
    }

    @Test
    fun `externalId doesnt sent if not set`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    externalId = null
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasNoJsonPath("$.params.prepared_data.external_id"))
    }
}
