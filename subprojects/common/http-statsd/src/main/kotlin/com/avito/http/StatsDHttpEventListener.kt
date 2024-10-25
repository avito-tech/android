package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import com.avito.graphite.series.SeriesName
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider

public class StatsDHttpEventListener(
    private val statsDSender: StatsDSender,
    timeProvider: TimeProvider,
    requestMetadataProvider: RequestMetadataProvider,
    loggerFactory: LoggerFactory,
) : MetricHttpEventListener(timeProvider, requestMetadataProvider, loggerFactory) {

    override fun send(seriesName: SeriesName, executionTimeMs: Long) {
        statsDSender.send(TimeMetric(seriesName, executionTimeMs))
    }
}
