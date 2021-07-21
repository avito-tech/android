package com.avito.runner.service

import com.avito.coroutines.extensions.Dispatchers
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.DeviceWorker
import com.avito.runner.service.worker.DeviceWorkerMessage
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.listener.CompositeDeviceListener
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.runner.service.worker.listener.DeviceLogListener
import com.avito.runner.service.worker.listener.MessagesDeviceListener
import com.avito.time.TimeProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.coroutineContext

internal class DeviceWorkerPoolImpl(
    private val outputDirectory: File,
    private val testListener: TestListener,
    private val deviceListener: DeviceListener,
    private val newDeviceListener: com.avito.runner.listener.DeviceListener,
    private val timeProvider: TimeProvider,
    private val deviceWorkersDispatcher: Dispatchers,
    private val state: DeviceWorkerPoolState,
    loggerFactory: LoggerFactory
) : DeviceWorkerPool {

    private val intentionsRouter: IntentionsRouter = IntentionsRouter(loggerFactory = loggerFactory)

    private val logger = loggerFactory.create<DeviceWorkerPool>()

    private val messages: Channel<DeviceWorkerMessage> =
        Channel(Channel.UNLIMITED)

    override suspend fun start() {
        with(CoroutineScope(coroutineContext)) {
            launch(CoroutineName("device-workers-pool")) {
                launch(CoroutineName("device-workers")) {
                    for (device in state.devices) {
                        DeviceWorker(
                            intentionsRouter = intentionsRouter,
                            device = device,
                            outputDirectory = outputDirectory,
                            testListener = testListener,
                            deviceListener = CompositeDeviceListener(
                                listOf(
                                    MessagesDeviceListener(messages),
                                    DeviceLogListener(device.logger),
                                    deviceListener
                                )
                            ),
                            newDeviceListener = newDeviceListener,
                            timeProvider = timeProvider,
                            dispatchers = deviceWorkersDispatcher
                        ).run()
                    }
                    throw IllegalStateException("devices channel was closed")
                }

                launch(CoroutineName("send-intentions")) {
                    for (intention in state.intentions) {
                        logger.debug("received intention: $intention")
                        intentionsRouter.sendIntention(intention = intention)
                    }
                }

                launch(CoroutineName("worker-messages")) {
                    for (message in messages) {
                        logger.debug("received message: $message")
                        when (message) {
                            is DeviceWorkerMessage.ApplicationInstalled ->
                                logger.debug("Application: ${message.installation.installation.application} installed")

                            is DeviceWorkerMessage.FailedIntentionProcessing -> {
                                logger.debug(
                                    "Received worker failed message during executing intention:" +
                                        " ${message.intention}. Rescheduling..."
                                )

                                intentionsRouter.sendIntention(intention = message.intention)
                            }

                            is DeviceWorkerMessage.Result ->
                                state.intentionResults.send(message.intentionResult)

                            is DeviceWorkerMessage.WorkerDied ->
                                state.deviceSignals.send(Device.Signal.Died(message.coordinate))
                        }
                    }
                }
            }
        }
    }

    override suspend fun stop() {
        intentionsRouter.cancel()
        messages.cancel()
        state.devices.cancel()
    }
}
