package com.avito.runner.trace

import com.avito.android.trace.DurationEvent
import com.avito.android.trace.TraceEvent
import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportFileAdapter
import com.avito.runner.model.DeviceId
import com.avito.time.TimeProvider
import java.io.File
import java.util.concurrent.TimeUnit

internal class DeviceCallbacksTraceReporter(
    private val timeProvider: TimeProvider,
    private val outputDirectory: File,
) : TraceReporter {

    private val state = mutableListOf<TraceEvent>()

    override fun report() {
        val traceReport = TraceReport(state)

        outputDirectory.mkdirs()
        val outputFile = File(outputDirectory, "report-new.trace")
        TraceReportFileAdapter(outputFile).write(traceReport)
    }

    override fun onDeviceCreated(deviceId: DeviceId) {
        state.add(
            DurationEvent(
                phase = DurationEvent.PHASE_BEGIN,
                timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(timeProvider.nowInMillis()),
                processId = deviceId.toString(),
                eventName = "waiting",
                color = TraceEvent.COLOR_YELLOW
            )
        )
    }

    override fun onIntentionReceived(deviceId: DeviceId) {
        state.add(
            DurationEvent(
                phase = DurationEvent.PHASE_END,
                timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(timeProvider.nowInMillis()),
                processId = deviceId.toString(),
                eventName = "waiting",
                color = TraceEvent.COLOR_YELLOW
            )
        )
    }
}
