package com.avito.impact.plugin.internal

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.isAndroidApp
import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.impact.ModifiedProject
import com.avito.impact.ModifiedProjectsFinder
import com.avito.math.percentOf
import com.avito.module.configurations.ConfigurationType
import com.avito.utils.gradle.Environment
import org.gradle.api.Project
import java.util.Locale

internal class ImpactMetricsSender(
    private val projectsFinder: ModifiedProjectsFinder,
    environmentInfo: EnvironmentInfo,
    private val metricTracker: BuildMetricTracker
) {

    init {
        require(environmentInfo.environment is Environment.CI) {
            "ImpactMetricsSender should run only in CI environment"
        }
    }

    private val ConfigurationType.name: String
        get() {
            return when (this) {
                ConfigurationType.AndroidTests -> "androidtests"
                ConfigurationType.Main -> "implementation"
                ConfigurationType.Lint -> "lint"
                ConfigurationType.UnitTests -> "unittests"
            }
        }

    fun sendMetrics() {
        val allProjects = projectsFinder.allProjects()

        ConfigurationType.values().forEach { type ->
            val modified = projectsFinder.modifiedProjects(type)

            sendModulesMetrics(type, allProjects, modified)
            sendAppsMetrics(type, allProjects, modified)
        }
    }

    private fun sendModulesMetrics(
        configurationType: ConfigurationType,
        projects: Set<Project>,
        modified: Set<ModifiedProject>
    ) {
        val metric = GaugeLongMetric(
            name = SeriesName.create("impact", "modules", configurationType.name.toLowerCase(Locale.US), "modified"),
            gauge = modified.size.percentOf(projects.size).roundToLong()
        )
        metricTracker.track(metric)
    }

    private fun sendAppsMetrics(
        configurationType: ConfigurationType,
        projects: Set<Project>,
        modified: Set<ModifiedProject>
    ) {
        val apps = projects.count { it.isAndroidApp() }
        val modifiedApps = modified.count { it.project.isAndroidApp() }

        val metric = GaugeLongMetric(
            name = SeriesName.create("impact", "apps", configurationType.name.toLowerCase(Locale.US), "modified"),
            gauge = modifiedApps.percentOf(apps).roundToLong()
        )
        metricTracker.track(metric)
    }
}
