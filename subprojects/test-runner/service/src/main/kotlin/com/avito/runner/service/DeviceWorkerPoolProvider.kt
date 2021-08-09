package com.avito.runner.service

import com.avito.coroutines.extensions.Dispatchers
import com.avito.logger.LoggerFactory
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.time.TimeProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.File

public class DeviceWorkerPoolProvider(
    private val testRunnerOutputDir: File,
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    private val deviceListener: DeviceListener,
    private val newDeviceListener: com.avito.runner.listener.DeviceListener,
    private val testListener: TestListener,
    private val intentions: Channel<Intention>,
    private val intentionResults: Channel<IntentionResult>,
    private val deviceSignals: Channel<Device.Signal>,
    private val dispatchers: Dispatchers = Dispatchers.SingleThread
) {

    public fun provide(
        devices: ReceiveChannel<Device>
    ): DeviceWorkerPool {
        return DeviceWorkerPoolImpl(
            outputDirectory = testRunnerOutputDir,
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
            testListener = testListener,
            deviceListener = deviceListener,
            newDeviceListener = newDeviceListener,
            state = DeviceWorkerPoolState(
                devices = devices,
                intentions = intentions,
                intentionResults = intentionResults,
                deviceSignals = deviceSignals
            ),
            deviceWorkersDispatcher = dispatchers
        )
    }
}
