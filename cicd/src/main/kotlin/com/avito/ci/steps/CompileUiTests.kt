package com.avito.ci.steps

import com.avito.android.withAndroidModule
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.AbstractCompile

class CompileUiTests(context: String) : SuppressibleBuildStep(context),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        project.withAndroidModule {
            project.tasks.withType<AbstractCompile>().forEach { compileTask ->
                val isForDebugBuild = compileTask.name.contains("debug", ignoreCase = true)
                if (isForDebugBuild) {
                    rootTask.configure { it.dependsOn(compileTask) }
                }
            }
        }
    }
}
