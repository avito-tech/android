package com.avito.runner.trace

import com.avito.runner.listener.DeviceListener
import com.avito.runner.listener.StubDeviceListener

public class StubTraceReporter(
    private val deviceListener: DeviceListener = StubDeviceListener()
) : TraceReporter,
    DeviceListener by deviceListener {

    override fun report() {
    }
}
