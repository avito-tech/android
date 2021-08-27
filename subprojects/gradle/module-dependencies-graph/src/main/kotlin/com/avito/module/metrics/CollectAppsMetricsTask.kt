package com.avito.module.metrics

import com.avito.logger.GradleLoggerFactory
import com.avito.logger.Logger
import com.avito.logger.create
import com.avito.module.internal.dependencies.AndroidAppsGraphBuilder
import com.avito.module.internal.dependencies.DependenciesGraphBuilder
import com.avito.module.metrics.metrics.AbsoluteMetrics
import com.avito.module.metrics.metrics.CollectAppsMetricsAction
import com.avito.module.metrics.metrics.Matrix
import com.avito.module.metrics.metrics.RelativeMetrics
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.util.Path
import java.io.File
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public abstract class CollectAppsMetricsTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    private val logger: Logger = GradleLoggerFactory.fromTask(this).create<CollectAppsMetricsTask>()

    init {
        description = "Collect Android applications modules health metrics"
    }

    private val outputDir = File(project.buildDir, "reports/app-modules-health")

    @OutputFile
    public val uniqueAppsMetrics: Property<RegularFile> = objects.fileProperty().apply {
        set(File(outputDir, "apps.csv"))
    }

    @OutputFile
    public val commonModulesComparison: Property<RegularFile> = objects.fileProperty().apply {
        set(File(outputDir, "apps-common-modules-comparison.csv"))
    }

    @OutputFile
    public val commonModulesDetails: Property<RegularFile> = objects.fileProperty().apply {
        set(File(outputDir, "apps-common-modules-details.log"))
    }

    @TaskAction
    public fun action() {
        val graphBuilder = DependenciesGraphBuilder(project.rootProject, GradleLoggerFactory.fromTask(this))
        val androidAppsGraphBuilder = AndroidAppsGraphBuilder(graphBuilder)

        val data = CollectAppsMetricsAction(androidAppsGraphBuilder).collect()
        writeAbsolute(data.absolute)
        writeRelative(data.relative)
        writeDetails(data.relative)

        logger.info("Wrote results to $outputDir")
    }

    private fun writeAbsolute(data: Map<Path, AbsoluteMetrics>) {
        val file = uniqueAppsMetrics.get().asFile
        file.writeText("module;all dependencies\n")
        data.forEach { (path, metrics) ->
            file.appendText("$path;${metrics.allDependencies}\n")
        }
    }

    private fun writeRelative(data: Matrix<Path, RelativeMetrics>) {
        val file = commonModulesComparison.get().asFile
        val columns: List<Path> = data.columnsCoordinates().toList()
        file.writeText(
            "module;" + columns.joinToString(separator = ";", postfix = "\n") { it.toString() }
        )
        data.rowsCoordinates().forEach { compared ->
            val values = columns
                .map { baseline ->
                    data.getOrNull(baseline, compared)
                }
                .joinToString(separator = ";", postfix = "\n") { metrics ->
                    metrics?.commonDependenciesRatio?.roundToInt().toString()
                }
            file.appendText("$compared;$values")
        }
    }

    private fun writeDetails(data: Matrix<Path, RelativeMetrics>) {
        val file = commonModulesDetails.get().asFile

        data.rowsCoordinates().forEach { comparedApp ->
            data.columnsCoordinates().map { baselineApp ->
                val metrics = data.getOrNull(baselineApp, comparedApp)
                if (metrics != null) {
                    val appDetails = """
                    |===
                    |Comparison of $baselineApp against $comparedApp
                    |
                    |$baselineApp has ${metrics.baselineDependencies.size} dependencies
                    |$comparedApp has ${metrics.comparedDependencies.size} dependencies
                    |
                    |${metrics.commonDependenciesRatio} of $baselineApp dependencies are common with $comparedApp
                    |
                    |Unique dependencies in $baselineApp: 
                    |${metrics.uniqueDependencies}
                    |
                    |Common dependencies between $baselineApp and $comparedApp: 
                    |${metrics.commonDependencies}
                    |""".trimMargin()
                    file.appendText(appDetails)
                }
            }
        }
    }
}
