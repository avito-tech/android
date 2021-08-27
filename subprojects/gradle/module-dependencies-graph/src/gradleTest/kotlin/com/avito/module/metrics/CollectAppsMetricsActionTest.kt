package com.avito.module.metrics

import com.avito.logger.StubLoggerFactory
import com.avito.module.internal.dependencies.AndroidAppsGraphBuilder
import com.avito.module.internal.dependencies.DependenciesGraphBuilder
import com.avito.module.metrics.metrics.AppsHealthData
import com.avito.module.metrics.metrics.CollectAppsMetricsAction
import com.avito.test.gradle.androidApp
import com.avito.test.gradle.androidLib
import com.avito.test.gradle.rootProject
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.util.Path
import org.junit.jupiter.api.Test

@Suppress("MagicNumber")
internal class CollectAppsMetricsActionTest {

    @Test
    fun `relative - no common modules`() {
        val root = rootProject()

        val lib = androidLib("lib", parent = root)

        val appA = androidApp("app-a", parent = root)
        appA.dependencies.add("implementation", lib)

        androidApp("app-b", parent = root)

        val relativeMetrics = collectMetrics(root).relative

        val aToB = relativeMetrics.getOrNull(Path.path(":app-a"), Path.path(":app-b"))
        assertThat(aToB!!.commonDependenciesRatio.roundToInt()).isEqualTo(0)

        val bToA = relativeMetrics.getOrNull(Path.path(":app-b"), Path.path(":app-a"))
        assertThat(bToA!!.commonDependenciesRatio.roundToInt()).isEqualTo(0)
    }

    @Test
    fun `relative - all common modules`() {
        val root = rootProject()

        val lib = androidLib("lib", parent = root)

        val appA = androidApp("app-a", parent = root)
        appA.dependencies.add("implementation", lib)

        val appB = androidApp("app-b", parent = root)
        appB.dependencies.add("implementation", lib)

        val relativeMetrics = collectMetrics(root).relative

        val aToB = relativeMetrics.getOrNull(Path.path(":app-a"), Path.path(":app-b"))
        assertThat(aToB!!.commonDependenciesRatio.roundToInt()).isEqualTo(100)

        val bToA = relativeMetrics.getOrNull(Path.path(":app-b"), Path.path(":app-a"))
        assertThat(bToA!!.commonDependenciesRatio.roundToInt()).isEqualTo(100)
    }

    @Test
    fun `relative - partial intersection`() {
        val root = rootProject()

        val libA = androidLib("libA", parent = root)
        val libB = androidLib("libB", parent = root)
        val libC = androidLib("libC", parent = root)
        val libD = androidLib("libD", parent = root)
        val libE = androidLib("libE", parent = root)
        val libF = androidLib("libF", parent = root)

        val appA = androidApp("app-a", parent = root)
        appA.dependencies.add("implementation", libA)
        appA.dependencies.add("implementation", libB)
        appA.dependencies.add("implementation", libC)
        appA.dependencies.add("implementation", libD)
        appA.dependencies.add("implementation", libE)

        val appB = androidApp("app-b", parent = root)
        appB.dependencies.add("implementation", libD)
        appB.dependencies.add("implementation", libE)
        appB.dependencies.add("implementation", libF)

        val relativeMetrics = collectMetrics(root).relative

        val aToB = relativeMetrics.getOrNull(Path.path(":app-a"), Path.path(":app-b"))
        assertThat(aToB!!.commonDependenciesRatio.roundToInt()).isEqualTo(40)

        val bToA = relativeMetrics.getOrNull(Path.path(":app-b"), Path.path(":app-a"))
        assertThat(bToA!!.commonDependenciesRatio.roundToInt()).isEqualTo(66)
    }

    private fun collectMetrics(root: Project): AppsHealthData {
        val graphBuilder = DependenciesGraphBuilder(root, StubLoggerFactory)
        val androidGraphBuilder = AndroidAppsGraphBuilder(graphBuilder)

        return CollectAppsMetricsAction(androidGraphBuilder).collect()
    }
}
