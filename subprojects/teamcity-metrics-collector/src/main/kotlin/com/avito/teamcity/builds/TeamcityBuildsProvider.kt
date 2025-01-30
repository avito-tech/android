package com.avito.teamcity.builds

import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.teamcity.TeamcityApi
import com.avito.teamcity.model.TeamcityMetricsSource
import org.jetbrains.teamcity.rest.Build
import java.time.Instant
import java.time.temporal.ChronoUnit

internal interface TeamcityBuildsProvider {
    fun provide(
        metricsSource: TeamcityMetricsSource,
        since: Instant,
        until: Instant,
    ): Sequence<Build>

    companion object {
        fun create(
            api: TeamcityApi,
            loggerFactory: LoggerFactory,
        ): TeamcityBuildsProvider {
            return Impl(api, loggerFactory)
        }
    }

    private class Impl(
        private val api: TeamcityApi,
        loggerFactory: LoggerFactory,
    ) : TeamcityBuildsProvider {

        private val logger: Logger = loggerFactory.create("TeamcityBuildsProvider")

        override fun provide(
            metricsSource: TeamcityMetricsSource,
            since: Instant,
            until: Instant,
        ): Sequence<Build> {
            logger.info("Provide builds for configuration ${metricsSource.configurationId}")
            logger.info("Provide builds end since $since until $until")
            val ranInFetchInterval = getBuilds(
                metricsSource.configurationId,
                since.minus(metricsSource.fetchIntervalInHours, ChronoUnit.HOURS),
                until
            )
            val endAtLastHour = ranInFetchInterval.filter { build ->
                val finishTime = requireNotNull(build.finishDateTime) {
                    "Can't be null. Because we don't fetch running builds"
                }
                finishTime.toInstant().isAfter(since)
            }

            logger.info("Found ${endAtLastHour.count()} builds")
            return endAtLastHour
        }

        private fun getBuilds(
            metricsSourceConfigurationId: String,
            since: Instant,
            until: Instant,
        ) = api.getBuilds(metricsSourceConfigurationId) {
            withAllBranches()
                .includeFailed()
                .includeCanceled()
                .since(since)
                .until(until)
        }
    }
}
