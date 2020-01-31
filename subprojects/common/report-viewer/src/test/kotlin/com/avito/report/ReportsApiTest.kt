package com.avito.report

import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.avito.test.gradle.fileFromJarResources
import com.github.salomonbrys.kotson.jsonObject
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.funktionale.tries.Try
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ReportsApiTest {

    private val mockWebServer = MockWebServer()
    private lateinit var reportsApi: ReportsApi

    @BeforeEach
    fun setup() {
        mockWebServer.start()
        val host = mockWebServer.url("/").toString()
        reportsApi = ReportsApi.create(
            host = host,
            fallbackUrl = "",
            logger = { message, error ->
                println(message)
                error?.printStackTrace()
            }
        )
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getReport - returns NotFound - when throws exception with no data`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"Internal error\",\"data\":\"not found\"},\"id\":1}")
        )

        val result = reportsApi.getReport(ReportCoordinates("AvitoAndroid", "FunctionalTests", "12345"))

        assertThat(result).isInstanceOf(GetReportResult.NotFound::class.java)
    }

    @Test
    fun `getReport - returns Error - when throws exception with no data`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = reportsApi.getReport(ReportCoordinates("AvitoAndroid", "FunctionalTests", "12345"))

        assertThat(result).isInstanceOf(GetReportResult.Error::class.java)
    }

    @Test
    fun `getReport - returns Report`() {
        mockWebServer.enqueue(MockResponse().setBody(fileFromJarResources<ReportsApiTest>("getReport.json").readText()))

        val result = reportsApi.getReport(ReportCoordinates("AvitoAndroid", "FunctionalTests", ""))

        assertThat(result).isInstanceOf(GetReportResult.Found::class.java)

        (result as GetReportResult.Found).report.run {
            // see json
            assertThat(id).isEqualTo("5c8032d5ccdf780001c49576")
        }
    }

    @Test
    fun `getTestsForRunId - returns ok`() {
        mockWebServer.enqueue(MockResponse().setBody(fileFromJarResources<ReportsApiTest>("getReport.json").readText()))
        mockWebServer.enqueue(MockResponse().setBody(fileFromJarResources<ReportsApiTest>("getTestsForRunId.json").readText()))

        val result = reportsApi.getTestsForRunId(ReportCoordinates("AvitoAndroid", "FunctionalTests", ""))

        assertThat(result).isInstanceOf(Try.Success::class.java)

        assertThat(result.get().first().name).isEqualTo("ru.domofond.features.RemoteToggleMonitorTest.check_remote_toggle")
    }

    @Test
    fun `pushPreparedData - returns ok`() {
        mockWebServer.enqueue(MockResponse().setBody(fileFromJarResources<ReportsApiTest>("pushPreparedData.json").readText()))

        val result = reportsApi.pushPreparedData("any", "any", jsonObject("any" to "any"))

        assertThat(result).isInstanceOf(Try.Success::class.java)
    }
}
