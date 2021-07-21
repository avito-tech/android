package com.avito.runner.model

public fun DeviceId.Companion.createStubInstance(serial: String = "12345"): DeviceId = DeviceId(serial)

public fun String.toDeviceId(): DeviceId = DeviceId.createStubInstance(this)
