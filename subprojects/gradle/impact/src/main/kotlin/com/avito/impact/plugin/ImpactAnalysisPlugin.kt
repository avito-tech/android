package com.avito.impact.plugin

import com.avito.git.gitState
import com.avito.impact.changes.newChangesDetector
import com.avito.impact.configuration.InternalModule
import com.avito.impact.configuration.internalModule
import com.avito.impact.impactFallbackDetector
import com.avito.kotlin.dsl.isRoot
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

@Suppress("UnstableApiUsage")
class ImpactAnalysisPlugin : Plugin<Project> {

    override fun apply(rootProject: Project) {
        check(rootProject.isRoot()) { "Plugin $pluginName should be applied to the root project" }

        rootProject.afterEvaluate {
            check(rootProject.subprojects.size > 0) { "Plugin $pluginName does not support single root-module projects" }
        }

        rootProject.extensions.create<ImpactAnalysisExtension>("impactAnalysis")

        val gitState = rootProject.gitState { rootProject.ciLogger.info(it) }

        val changesDetector = newChangesDetector(
            rootDir = rootProject.rootDir,
            targetCommit = gitState.orNull?.targetBranch?.commit,
            logger = rootProject.ciLogger
        )

        rootProject.subprojects.forEach { subProject ->
            subProject.internalModule = InternalModule(
                project = subProject,
                changesDetector = changesDetector,
                fallbackDetector = subProject.impactFallbackDetector
            )
        }

        rootProject.tasks.register<GenerateModulesReport>("generateModulesReport") {
            group = tasksGroup
            description = "Print modified projects to files. For testing purposes only"
        }

        rootProject.tasks.register<ImpactMetricsTask>("impactMetrics") {
            group = tasksGroup
            description = "Sends impact analysis metrics to graphite"
        }
    }
}

private const val tasksGroup = "impact-analysis"
private const val pluginName = "ImpactAnalysisPlugin"
