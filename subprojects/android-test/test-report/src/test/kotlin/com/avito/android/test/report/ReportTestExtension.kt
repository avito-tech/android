package com.avito.android.test.report

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.test.report.future.MockFutureValue
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.performance.PerformanceTestReporter
import com.avito.android.test.report.screenshot.ScreenshotUploader
import com.avito.android.test.report.transport.LocalRunTransport
import com.avito.filestorage.RemoteStorage
import com.avito.logger.NoOpLogger
import com.avito.logger.Logger
import com.avito.report.model.DeviceName
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.ReportCoordinates
import com.avito.time.TimeProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.mock.MockInterceptor
import okhttp3.mock.Rule
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * @param sendRealReport позволяет на время отладки посылать реальные репорты во время тестов,
 *                       чтобы посмотреть как оно отображается
 */
class ReportTestExtension(
    // todo useless because report urls are empty
    val sendRealReport: Boolean = false,
    val timeProvider: TimeProvider = mock(),
    val fileStorageUrl: String = "https://filestorage.com",
    private val mockInterceptor: MockInterceptor = MockInterceptor(),
    private val screenshotUploader: ScreenshotUploader = mock(),
    private val logger: Logger = NoOpLogger,
    private val testRunCoordinates: ReportCoordinates = ReportCoordinates(
        planSlug = "android-test",
        jobSlug = "android-test",
        runId = "android-test5"
    ),
    private val deviceName: String = "android-test",
    private val report: Report = ReportImplementation(
        fileStorageUrl = fileStorageUrl,
        onDeviceCacheDirectory = lazy { error("nope") },
        httpClient = OkHttpClient.Builder()
            .apply {
                if (!sendRealReport) {
                    addInterceptor(mockInterceptor)
                }
            }
            .build(),
        performanceTestReporter = PerformanceTestReporter(),
        logger = logger,
        transport = if (sendRealReport) listOf(
            LocalRunTransport(
                reportApiHost = "", // TODO required?
                reportFallbackUrl = "", // TODO required
                reportViewerUrl = "", // TODO required
                reportCoordinates = testRunCoordinates,
                deviceName = DeviceName(deviceName),
                logger = logger
            )
        ) else emptyList(),
        screenshotUploader = screenshotUploader,
        timeProvider = timeProvider
    )
) : BeforeEachCallback, Report by report {

    override fun beforeEach(context: ExtensionContext) {
        mockInterceptor.addRule(
            Rule.Builder()
                .post()
                .urlStarts(fileStorageUrl)
                .respond(200)
                .body(ResponseBody.run { "uri\":\"a\"".toResponseBody() })
        )
        whenever(screenshotUploader.makeAndUploadScreenshot(any()))
            .thenReturn(
                MockFutureValue(
                    RemoteStorage.Result.Error(
                        RuntimeException()
                    )
                )
            )
    }

    fun initTestCaseHelper(
        testCaseId: Int? = null,
        testClass: String = "com.avito.test.Test",
        testMethod: String = "test",
        testDescription: String? = null,
        dataSetNumber: Int? = null,
        kind: Kind = Kind.UNKNOWN,
        externalId: String? = null,
        tagIds: List<Int> = emptyList(),
        featureIds: List<Int> = emptyList(),
        featuresFromAnnotation: List<String> = emptyList(),
        featuresFromPackage: List<String> = emptyList(),
        priority: TestCasePriority? = null,
        behavior: TestCaseBehavior? = null,
        flakiness: Flakiness = Flakiness.Stable
    ) {
        initTestCase(
            testMetadata = TestMetadata(
                caseId = testCaseId,
                description = testDescription,
                className = testClass,
                methodName = testMethod,
                dataSetNumber = dataSetNumber,
                kind = kind,
                priority = priority,
                behavior = behavior,
                externalId = externalId,
                featureIds = featureIds,
                tagIds = tagIds,
                flakiness = flakiness
            )
        )
    }
}
