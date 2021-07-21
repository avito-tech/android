package com.avito.runner.scheduler.metrics

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.model.DeviceId
import com.avito.runner.scheduler.metrics.model.DeviceTimestamps
import com.avito.runner.scheduler.metrics.model.TestKey
import com.avito.runner.scheduler.metrics.model.TestTimestamps
import com.avito.runner.scheduler.metrics.model.finish
import com.avito.runner.scheduler.metrics.model.start
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.time.TimeProvider
import java.util.concurrent.ConcurrentHashMap

internal class TestMetricsListenerImpl(
    private val testMetricsSender: TestMetricsSender,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : TestSuiteListener, DeviceListener {

    private val logger = loggerFactory.create<TestSuiteListener>()

    private val deviceTimestamps = ConcurrentHashMap<DeviceId, DeviceTimestamps>()

    private var testSuiteStartedTime: Long = 0

    override fun onTestSuiteStarted() {
        testSuiteStartedTime = timeProvider.nowInMillis()
    }

    override suspend fun onDeviceCreated(device: Device, state: State) {
        deviceTimestamps[device.toId()] = DeviceTimestamps.Started(
            created = timeProvider.nowInMillis(),
            testTimestamps = mutableMapOf(),
        )
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
        deviceTimestamps.compute(device.toId()) { _: DeviceId, oldValue: DeviceTimestamps? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testTimestamps[intention.testKey()] = TestTimestamps.NotStarted(timeProvider.nowInMillis())
                oldValue
            }
        }
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
        // empty
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
        // empty
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
        deviceTimestamps.compute(device.toId()) { _: DeviceId, oldValue: DeviceTimestamps? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testTimestamps.compute(intention.testKey()) { _: TestKey, testTimestamps: TestTimestamps? ->
                    if (testTimestamps == null) {
                        logger.warn(
                            "Fail to set timestamp value, previous required values not found, " +
                                "this shouldn't happen"
                        )
                        null
                    } else {
                        testTimestamps.start(timeProvider.nowInMillis())
                    }
                }
                oldValue
            }
        }
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
        deviceTimestamps.compute(device.toId()) { _: DeviceId, oldValue: DeviceTimestamps? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testTimestamps.compute(intention.testKey()) { _: TestKey, testTimestamps: TestTimestamps? ->
                    if (testTimestamps == null) {
                        logger.warn(
                            "Fail to set timestamp value, previous required values not found, " +
                                "this shouldn't happen"
                        )
                        null
                    } else {
                        testTimestamps.finish(timeProvider.nowInMillis())
                    }
                }
                oldValue
            }
        }
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
        // empty
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
        // alternative to onFinished terminated state
    }

    override suspend fun onFinished(device: Device) {
        deviceTimestamps.compute(device.toId()) { _: DeviceId, oldValue: DeviceTimestamps? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.finish(finished = timeProvider.nowInMillis())
            }
        }
    }

    override fun onTestSuiteFinished() {
        val aggregator: TestMetricsAggregator = createTestMetricsAggregator()

        with(testMetricsSender) {
            aggregator.initialDelay().fold(
                { sendInitialDelay(it) },
                { logger.warn("Not sending initial delay, no data") }
            )

            aggregator.medianQueueTime().fold(
                { sendMedianQueueTime(it) },
                { logger.warn("Not sending median test queue time, no data") }
            )
            aggregator.medianInstallationTime().fold(
                { sendMedianInstallationTime(it) },
                { logger.warn("Not sending median test start time, no data") }
            )

            aggregator.endDelay().fold(
                { sendEndDelay(it) },
                { logger.warn("Not sending end delay, no data") }
            )

            aggregator.suiteTime().fold(
                { sendSuiteTime(it) },
                { logger.warn("Not sending suite time, no data") }
            )

            sendTotalTime(aggregator.totalTime())

            aggregator.medianDeviceUtilization().fold(
                { sendMedianDeviceUtilization(it.toInt()) },
                { logger.warn("Not sending median device relative wasted time, no data") }
            )
        }
    }

    private fun Device.toId() = DeviceId(coordinate.serial.value)

    private fun Intention.testKey() = TestKey(action.test, action.executionNumber)

    private fun createTestMetricsAggregator(): TestMetricsAggregator {
        return TestMetricsAggregatorImpl(
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = timeProvider.nowInMillis(),
            deviceTimestamps = deviceTimestamps
        )
    }
}
