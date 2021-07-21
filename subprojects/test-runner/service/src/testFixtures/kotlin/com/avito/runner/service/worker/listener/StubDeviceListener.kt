package com.avito.runner.service.worker.listener

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation

public class StubDeviceListener : DeviceListener {

    public var isDeviceCreated: Boolean = false
        private set

    public var isFinished: Boolean = false
        private set

    override suspend fun onDeviceCreated(device: Device, state: State) {
        isDeviceCreated = true
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
        // empty
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
        // empty
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
        // empty
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
        // empty
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
        // empty
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
        // empty
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
        // empty
    }

    override suspend fun onFinished(device: Device) {
        isFinished = true
    }
}
