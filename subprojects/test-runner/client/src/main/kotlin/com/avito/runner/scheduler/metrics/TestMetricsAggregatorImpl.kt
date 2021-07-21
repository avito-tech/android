package com.avito.runner.scheduler.metrics

import com.avito.android.Result
import com.avito.math.median
import com.avito.runner.model.DeviceId
import com.avito.runner.scheduler.metrics.model.DeviceTimestamps
import com.avito.runner.scheduler.metrics.model.TestTimestamps

internal data class TestMetricsAggregatorImpl(
    private val testSuiteStartedTime: Long,
    private val testSuiteEndedTime: Long,
    private val deviceTimestamps: Map<DeviceId, DeviceTimestamps>
) : TestMetricsAggregator {

    private val testTimestamps = deviceTimestamps.flatMap { it.value.testTimestamps.values }

    private val firstTestStarted: Result<Long> = testTimestamps.filterIsInstance<TestTimestamps.Finished>()
        .map { it.startTime }
        .minOrNull()
        .toResult { "Cannot calculate first started test time" }

    private val lastTestEnded: Result<Long> = testTimestamps.filterIsInstance<TestTimestamps.Finished>()
        .map { it.finishTime }
        .maxOrNull()
        .toResult { "Cannot calculate last ended test time" }

    private val queueTimes: List<Long> = testTimestamps.filterIsInstance<TestTimestamps.Finished>()
        .map { it.onDevice - testSuiteStartedTime }

    private val installationTimes: List<Long> = testTimestamps.filterIsInstance<TestTimestamps.Finished>()
        .map { it.installationTime }

    override fun initialDelay(): Result<Long> = firstTestStarted.map { it - testSuiteStartedTime }

    override fun endDelay(): Result<Long> = lastTestEnded.map { testSuiteEndedTime - it }

    override fun medianQueueTime(): Result<Long> = queueTimes.aggregate(
        { it.median() },
        { "Cannot calculate median queue time" }
    )

    override fun medianInstallationTime(): Result<Long> = installationTimes.aggregate(
        { it.median() },
        { "Cannot calculate median installation time" }
    )

    override fun suiteTime(): Result<Long> = lastTestEnded.combine(firstTestStarted) { last, first -> last - first }

    override fun totalTime() = testSuiteEndedTime - testSuiteStartedTime

    override fun medianDeviceUtilization(): Result<Long> =
        deviceTimestamps.values.filterIsInstance<DeviceTimestamps.Finished>()
            .map { it.utilizationPercent }
            .aggregate({ it.median() }) { "Cannot calculate median device utilization" }

    private inline fun Long?.toResult(lazyMessage: () -> String): Result<Long> = when (this) {
        null -> Result.Failure(IllegalStateException(lazyMessage()))
        else -> Result.Success(this)
    }

    private inline fun List<Number>.aggregate(
        aggregateFunc: (List<Number>) -> Number,
        lazyMessage: () -> String
    ): Result<Long> {
        return when {
            this.isEmpty() -> Result.Failure(IllegalStateException(lazyMessage()))
            else -> Result.Success(aggregateFunc(this).toLong())
        }
    }
}
