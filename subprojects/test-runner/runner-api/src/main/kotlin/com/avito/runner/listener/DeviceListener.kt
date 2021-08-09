package com.avito.runner.listener

import com.avito.runner.model.DeviceId
import com.avito.runner.model.TestCaseRun
import com.avito.test.model.TestCase

public interface DeviceListener {

    public fun onCreate(deviceId: DeviceId)

    public fun onReady(deviceId: DeviceId)

    public fun onDie(deviceId: DeviceId)

    public fun onPrepareStateStart(deviceId: DeviceId)

    public fun onPrepareStateFail(deviceId: DeviceId)

    public fun onPrepareStateSuccess(deviceId: DeviceId)

    public fun onTestStarted(deviceId: DeviceId, testCase: TestCase)

    public fun onTestFinished(deviceId: DeviceId, testCaseRun: TestCaseRun)

    public fun onTestActionFailure(deviceId: DeviceId, testCase: TestCase)
}
