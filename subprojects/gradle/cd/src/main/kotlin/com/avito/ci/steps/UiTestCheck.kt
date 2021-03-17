package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.configuration.internalModule
import com.avito.instrumentation.instrumentationTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class UiTestCheck(context: String, name: String) : SuppressibleBuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl(),
    FlakyAwareBuildStep by FlakyAwareBuildStep.Impl() {

    // public, used in build scripts
    var configurations = mutableListOf<String>()

    @Suppress("unused") // used in build scripts
    fun configurations(vararg configs: String) {
        configurations.addAll(configs)
    }

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (useImpactAnalysis && !project.internalModule.isModified()) return

        val checkTask = project.tasks.register<Task>("${context}InstrumentationTest") {
            group = cdTaskGroup
            description = "Run all instrumentation tests needed for $context"

            configurations.forEach { configuration ->

                val uiTestTask = project.tasks.instrumentationTask(configuration)

                // it is safe to call get() here because task instrumentationXXX must be ready here
                // can't configure task in registration of another
                uiTestTask.get().also { task ->
                    task.suppressFailure.set(this@UiTestCheck.suppressFailures)
                    task.suppressFlaky.set(this@UiTestCheck.suppressFlaky)
                }

                dependsOn(uiTestTask)
            }
        }

        rootTask.dependsOn(checkTask)
    }
}
