package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage

/**
 * Save report to extrnal storage
 * Send all data to remote storage
 *
 * Legacy way; see [ExternalStorageTransport]
 */
class LegacyTransport(
    private val uploadToAvitoRemoteStorageTransport: UploadToAvitoRemoteStorageTransport,
    private val externalStorageTransport: ExternalStorageTransport
) : Transport, PreTransportMappers {

    override fun sendReport(state: ReportState.Initialized.Started) {
        // todo handle result
        externalStorageTransport.sendReport(state)
    }

    override fun sendContent(
        test: TestMetadata,
        request: RemoteStorage.Request,
        comment: String
    ): FutureValue<RemoteStorage.Result> {
        return uploadToAvitoRemoteStorageTransport.sendContent(test, request, comment)
    }
}
