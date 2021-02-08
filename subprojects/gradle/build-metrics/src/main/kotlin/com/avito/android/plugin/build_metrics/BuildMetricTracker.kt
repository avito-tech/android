package com.avito.android.plugin.build_metrics

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric
import org.gradle.BuildResult
import org.gradle.api.provider.Provider

class BuildMetricTracker(
    private val env: Provider<EnvironmentInfo>,
    private val sender: Provider<StatsDSender>
) {

    private val node by lazy {
        env.get().node?.take(32) ?: "unknown"
    }

    fun track(buildResult: BuildResult, metric: StatsMetric) {
        val prefix = SeriesName.create(
            env.get().environment.publicName,
            node,
            "id",
            buildStatus(buildResult)
        )
        sender.get().send(prefix, metric)
    }

    fun track(metric: StatsMetric) {
        val prefix = SeriesName.create(
            env.get().environment.publicName,
            node,
            "id"
        )
        sender.get().send(prefix, metric)
    }

    private fun buildStatus(buildResult: BuildResult): String {
        return if (buildResult.failure == null) "success" else "fail"
    }
}
