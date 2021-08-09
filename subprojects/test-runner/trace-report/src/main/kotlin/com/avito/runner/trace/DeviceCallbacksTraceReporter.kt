package com.avito.runner.trace

import com.avito.android.trace.DurationEvent
import com.avito.android.trace.TraceEvent
import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportFileAdapter
import com.avito.runner.model.DeviceId
import com.avito.runner.model.TestCaseRun
import com.avito.test.model.TestCase
import com.avito.time.TimeProvider
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

internal class DeviceCallbacksTraceReporter(
    private val timeProvider: TimeProvider,
    private val outputDirectory: File,
) : TraceReporter {

    private val state = ConcurrentLinkedQueue<TraceEvent>()

    override fun report() {
        val traceReport = TraceReport(state.toList())

        outputDirectory.mkdirs()
        val outputFile = File(outputDirectory, "report-new.trace")
        TraceReportFileAdapter(outputFile).write(traceReport)
    }

    override fun onCreate(deviceId: DeviceId) {
        trackBegin(deviceId, "get-status")
    }

    override fun onReady(deviceId: DeviceId) {
        trackEnd(deviceId, "get-status", TraceEvent.COLOR_ORANGE)
    }

    override fun onDie(deviceId: DeviceId) {
        trackEnd(deviceId, "get-status", TraceEvent.COLOR_LIGHT_RED)
    }

    override fun onPrepareStateStart(deviceId: DeviceId) {
        trackBegin(deviceId, "prepare-state")
    }

    override fun onPrepareStateSuccess(deviceId: DeviceId) {
        trackEnd(deviceId, "prepare-state", TraceEvent.COLOR_BLUE)
    }

    override fun onPrepareStateFail(deviceId: DeviceId) {
        trackEnd(deviceId, "prepare-state", TraceEvent.COLOR_LIGHT_RED)
    }

    override fun onTestStarted(deviceId: DeviceId, testCase: TestCase) {
        trackBegin(deviceId, testCase.toString())
    }

    override fun onTestFinished(deviceId: DeviceId, testCaseRun: TestCaseRun) {
        val color = when (testCaseRun.result) {
            is TestCaseRun.Result.Passed -> TraceEvent.COLOR_LIGHT_GREEN
            is TestCaseRun.Result.Failed -> TraceEvent.COLOR_LIGHT_RED
            TestCaseRun.Result.Ignored -> TraceEvent.COLOR_GREY
        }
        trackEnd(deviceId, testCaseRun.test.toString(), color)
    }

    override fun onTestActionFailure(deviceId: DeviceId, testCase: TestCase) {
        trackEnd(deviceId, testCase.toString(), TraceEvent.COLOR_BAD)
    }

    private fun trackBegin(deviceId: DeviceId, eventName: String) {
        state.add(
            DurationEvent(
                phase = DurationEvent.PHASE_BEGIN,
                timestampMicroseconds = now(),
                processId = deviceId.toString(),
                eventName = eventName,
            )
        )
    }

    private fun trackEnd(deviceId: DeviceId, eventName: String, color: String) {
        state.add(
            DurationEvent(
                phase = DurationEvent.PHASE_END,
                timestampMicroseconds = now(),
                processId = deviceId.toString(),
                eventName = eventName,
                color = color
            )
        )
    }

    private fun now(): Long {
        return TimeUnit.MILLISECONDS.toMicros(timeProvider.nowInMillis())
    }
}
