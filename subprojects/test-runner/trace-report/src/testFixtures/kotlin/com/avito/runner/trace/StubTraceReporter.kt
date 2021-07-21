package com.avito.runner.trace

import com.avito.runner.model.DeviceId

public class StubTraceReporter : TraceReporter {

    override fun report() {
    }

    override fun onDeviceCreated(deviceId: DeviceId) {
    }

    override fun onIntentionReceived(deviceId: DeviceId) {
    }
}
