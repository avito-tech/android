package com.avito.runner.listener

import com.avito.runner.model.DeviceId

public class StubDeviceListener : DeviceListener {

    override fun onDeviceCreated(deviceId: DeviceId) {
    }

    override fun onIntentionReceived(deviceId: DeviceId) {
    }
}
