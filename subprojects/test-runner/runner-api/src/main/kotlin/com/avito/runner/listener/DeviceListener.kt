package com.avito.runner.listener

import com.avito.runner.model.DeviceId

public interface DeviceListener {

    public fun onDeviceCreated(deviceId: DeviceId)

    public fun onIntentionReceived(deviceId: DeviceId)
}
