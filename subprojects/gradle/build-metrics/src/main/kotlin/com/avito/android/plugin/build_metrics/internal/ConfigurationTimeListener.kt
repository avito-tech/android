package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.plugin.build_metrics.BuildMetricTracker
import com.avito.android.stats.SeriesName
import com.avito.android.stats.TimeMetric

internal class ConfigurationTimeListener(
    private val metricTracker: BuildMetricTracker
) : BuildResultListener {

    override fun onBuildFinished(status: BuildStatus, profile: BuildProfile) {
        metricTracker.track(
            status,
            TimeMetric(SeriesName.create("init_configuration", "total"), profile.initWithConfigurationTimeMs)
        )
    }
}
