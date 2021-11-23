package com.avito.android.module_type

import com.avito.android.module_type.internal.CheckModuleDependenciesTask
import com.avito.android.module_type.internal.ExtractModuleDescriptionTask
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.logger.GradleLoggerPlugin
import com.avito.module.configurations.ConfigurationType
import com.avito.module.dependencies.directDependenciesOnProjects
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.avito.android.ModuleTypeExtension as LegacyModuleTypeExtension

public class ModuleTypesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project.isRoot()) {
            configureRootProject(project)
        } else {
            if (project.useCustomTypes) {
                configureNonRootProject(project)
            } else {
                configureLegacyNonRootProject(project)
            }
        }
    }

    private fun configureRootProject(project: Project) {
        val extension = project.extensions.create(
            ModuleTypeRootExtension.name,
            ModuleTypeRootExtension::class.java,
        )
        project.tasks.register(
            CheckModuleDependenciesTask.name,
            CheckModuleDependenciesTask::class.java
        ) { task ->
            task.group = "verification"
            task.severity.set(extension.severity)
            task.restrictions.set(extension.restrictions)
            task.loggerFactory.set(GradleLoggerPlugin.getLoggerFactory(task))
        }
    }

    private fun configureNonRootProject(project: Project) {
        val extension = project.extensions.create(
            "module",
            ModuleTypeExtension::class.java,
        )
        val checksTask = project.rootProject.tasks.typedNamedOrNull<CheckModuleDependenciesTask>(
            CheckModuleDependenciesTask.name
        )
        requireNotNull(checksTask) {
            "Plugin must be applied to the root project also"
        }
        checksTask.configure {
            it.dependsOn("${project.path}:${ExtractModuleDescriptionTask.name}")
            // Workaround for project isolation to wire tasks
            // - We can't read task's dependencies in execution phase
            // - We can't wire them through output/input because to get task provider we need to get a project
            it.dependentProjects.set(
                mutableSetOf(project.path) + it.dependentProjects.get()
            )
        }

        project.tasks.register(
            ExtractModuleDescriptionTask.name,
            ExtractModuleDescriptionTask::class.java
        ) { task ->
            task.module.set(ModuleWithType(project.path, extension.type.orNull))
            task.outputFile.set(
                project.layout.buildDirectory.file(ExtractModuleDescriptionTask.outputPath)
            )
            val directDependencies = project.directDependenciesOnProjects(ConfigurationType.values().toSet())
                .mapValues { it.value.map { it.path }.toSet() }
            task.directDependencies.set(directDependencies)
        }
    }

    private fun configureLegacyNonRootProject(project: Project) {
        project.extensions.create(
            "module",
            LegacyModuleTypeExtension::class.java,
        )
    }
}

internal const val pluginId = "com.avito.android.module-types"

// TODO: delete after migrating clients in MBS-12266
private val Project.useCustomTypes: Boolean
    get() = providers
        .gradleProperty("avito.module_type.useCustomTypes")
        .forUseAtConfigurationTime()
        .map { it.toBoolean() }
        .getOrElse(false)