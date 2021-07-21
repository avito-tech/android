package com.avito.runner.listener

import com.avito.runner.model.DeviceId
import com.avito.runner.model.TestCaseRun
import com.avito.test.model.TestCase

public class StubDeviceListener : DeviceListener {

    override fun onCreate(deviceId: DeviceId) {
    }

    override fun onReady(deviceId: DeviceId) {
    }

    override fun onDie(deviceId: DeviceId) {
    }

    override fun onPrepareStateStart(deviceId: DeviceId) {
    }

    override fun onPrepareStateFail(deviceId: DeviceId) {
    }

    override fun onPrepareStateSuccess(deviceId: DeviceId) {
    }

    override fun onTestStarted(deviceId: DeviceId, testCase: TestCase) {
    }

    override fun onTestFinished(deviceId: DeviceId, testCaseRun: TestCaseRun) {
    }

    override fun onTestActionFailure(deviceId: DeviceId, testCase: TestCase) {
    }
}
