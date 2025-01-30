package com.avito.teamcity

import com.avito.android.graphite.GraphiteSender
import com.avito.teamcity.builds.PreviousMetricsSendingTimeProvider
import com.avito.teamcity.builds.TeamcityBuildsProvider
import com.avito.teamcity.metric.TeamcityBuildDurationMetric
import com.avito.teamcity.metric.TeamcityBuildQueueMetric
import com.avito.teamcity.model.TeamcityMetricsSource
import org.jetbrains.teamcity.rest.Build
import java.time.Instant

internal class SendTeamcityBuildMetricsAction(
    private val teamcityBuildsProvider: TeamcityBuildsProvider,
    private val previousMetricsSendingTimeProvider: PreviousMetricsSendingTimeProvider,
    private val graphiteSender: GraphiteSender,
) {

    fun execute(metricsSources: List<TeamcityMetricsSource>) {
        val since = previousMetricsSendingTimeProvider.getPreviousSendingTime()
        val until = Instant.now()
        metricsSources.forEach { metricsSource ->
            teamcityBuildsProvider.provide(
                metricsSource = metricsSource,
                since = since,
                until = until,
            ).forEach { build: Build ->
                graphiteSender.send(
                    TeamcityBuildQueueMetric(build).asGraphite()
                )
                graphiteSender.send(
                    TeamcityBuildDurationMetric(build).asGraphite()
                )
                log(build)
            }
        }
        previousMetricsSendingTimeProvider.saveSendingTime(until)
    }

    private fun log(build: Build) {
        println(
            buildString {
                appendLine("Sent build metrics for ${build.buildNumber}")
                appendLine("Status ${build.status}")
                appendLine("queued at ${build.queuedDateTime}")
                appendLine("start at ${build.startDateTime}")
                appendLine("finished at ${build.finishDateTime}")
            }
        )
    }
}
