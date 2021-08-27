package com.avito.android.transport

import com.avito.android.test.report.transport.ExternalStorageTransport
import com.avito.android.test.report.transport.NoOpTransport
import com.avito.android.test.report.transport.Transport
import com.avito.filestorage.RemoteStorage
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.TestArtifactsProvider
import com.avito.report.serialize.ReportSerializer
import com.avito.reportviewer.ReportsApiFactory
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.model.DeviceName
import com.avito.time.TimeProvider

class ReportTransportFactory(
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    private val remoteStorage: RemoteStorage,
    private val httpClientProvider: HttpClientProvider,
    reportSerializer: ReportSerializer,
    testArtifactsProvider: TestArtifactsProvider
) {

    private val logger = loggerFactory.create<ReportTransportFactory>()

    private val externalStorageTransport = ExternalStorageTransport(
        timeProvider = timeProvider,
        loggerFactory = loggerFactory,
        testArtifactsProvider = testArtifactsProvider,
        reportSerializer = reportSerializer
    )

    fun create(
        testRunCoordinates: ReportCoordinates,
        reportDestination: ReportDestination
    ): Transport {

        logger.info("reportDestination=$reportDestination")

        val uploadFromDevice = AvitoRemoteStorageTransport(remoteStorage, timeProvider)

        return when (reportDestination) {
            is ReportDestination.Backend -> LocalRunTransport(
                reportViewerUrl = reportDestination.reportViewerUrl,
                reportCoordinates = testRunCoordinates,
                deviceName = DeviceName(reportDestination.deviceName),
                loggerFactory = loggerFactory,
                reportsApi = ReportsApiFactory.create(
                    host = reportDestination.reportApiUrl,
                    loggerFactory = loggerFactory,
                    httpClientProvider = httpClientProvider
                ),
                remoteStorageTransport = uploadFromDevice
            )
            is ReportDestination.Legacy -> LegacyTransport(
                remoteStorageTransport = uploadFromDevice,
                externalStorageTransport = externalStorageTransport
            )
            ReportDestination.File -> externalStorageTransport
            ReportDestination.NoOp -> NoOpTransport
        }
    }
}
