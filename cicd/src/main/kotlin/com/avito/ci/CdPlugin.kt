package com.avito.ci

import com.avito.ci.steps.BuildStep.Scope
import com.avito.impact.configuration.internalModule
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

@Suppress("unused", "UnstableApiUsage")
class CdPlugin : Plugin<Project> {

    private val taskGroup = "cd"

    override fun apply(project: Project) {

        //todo создавать типы билдов динамически
        val config = project.extensions.create<CiCdExtension>("builds", project.objects)

        // TODO: MBS-6118. В CD во время конфигурации активно используется git. Это слишком долго.
        // Локально не создаем эти таски для ускорения конфигурации
        if (project.buildEnvironment !is BuildEnvironment.CI) return

        val localCheckTask = project.tasks.register<Task>("localCheck") {
            group = taskGroup
            description = "Fast local checks with impact analysis"
        }

        val releaseTask = project.tasks.register<Task>("release") {
            group = taskGroup
            description = "Task to build for release (runs full non-blocking full regression suite)"
        }

        val fullCheckTask = project.tasks.register<Task>("fullCheck") {
            group = taskGroup
            description = "Task to run all specified check on project"
        }

        val fastCheckTask = project.tasks.register<Task>("fastCheck") {
            group = taskGroup
            description = "Task to run fast check of project. Based on impact analysis"
        }

        project.gradle.projectsEvaluated {
            registerTask(project, config.localCheckSteps, localCheckTask)
            registerTask(project, config.releaseSteps, releaseTask)
            registerTask(project, config.fullCheckSteps, fullCheckTask)
            registerTask(project, config.fastCheckSteps, fastCheckTask)
        }
    }

    private fun registerTask(project: Project, buildSteps: BuildStepListExtension, rootTask: TaskProvider<Task>) {
        // TODO: Move heavy check from configuration to execution phase (onlyIf)
        val runApplicationChecks by lazy { !buildSteps.useImpactAnalysis || project.internalModule.isModified() }

        buildSteps.steps.get().forEach { step ->
            val useStep = when (step.scope) {
                Scope.APPLICATION -> runApplicationChecks
                Scope.ROOT_PROJECT -> true
            }
            if (useStep) {
                step.registerTask(project, rootTask)
            }
        }
    }
}
