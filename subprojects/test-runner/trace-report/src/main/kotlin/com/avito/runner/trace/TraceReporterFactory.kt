package com.avito.runner.trace

import com.avito.time.TimeProvider
import java.io.File

public object TraceReporterFactory {

    public fun create(
        timeProvider: TimeProvider,
        outputDirectory: File
    ): TraceReporter {
        return DeviceCallbacksTraceReporter(
            timeProvider = timeProvider,
            outputDirectory = outputDirectory
        )
    }
}
