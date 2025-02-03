@file:Suppress("UnstableApiUsage")

package com.avito.logger

import com.avito.logger.builder.GradleLoggerFactoryBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

public class GradleLoggerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        checkProjectIsRoot(target)
        val extension = target.extensions.create("gradleLogger", GradleLoggerExtension::class.java)
        extension.fileHandlerRootDir.set(target.layout.buildDirectory.dir("logs"))
        target.afterEvaluate {
            /**
             * The problem is some plugins could start use LoggerService before build script evaluated
             * It leads to state when we initialize LoggerService with empty [GradleLoggerExtension]
             */
            extension.finalizeValues()
        }
    }

    public companion object {

        internal const val error = "com.avito.android.gradle-logger plugin must be added to the root project"

        public fun provideLoggerFactoryBuilder(task: Task): Provider<GradleLoggerFactoryBuilder> {
            val project = task.project
            return getLoggerFactoryBuilder(project, GradleLoggerCoordinates(project.path, task.name))
        }

        public fun provideLoggerFactoryBuilder(
            project: Project
        ): Provider<GradleLoggerFactoryBuilder> =
            getLoggerFactoryBuilder(project, GradleLoggerCoordinates(project.path))

        public fun provideLoggerFactory(
            task: Task
        ): Provider<LoggerFactory> {
            return provideLoggerFactoryBuilder(task).map { it.build() }
        }

        public fun provideLoggerFactory(
            project: Project
        ): Provider<LoggerFactory> =
            provideLoggerFactoryBuilder(project).map { it.build() }

        public fun getLoggerFactory(task: Task): LoggerFactory = LazyLoggerFactory(provideLoggerFactory(task))
        public fun getLoggerFactory(project: Project): LoggerFactory = LazyLoggerFactory(provideLoggerFactory(project))

        private fun getLoggerService(project: Project): Provider<LoggerService> {
            val rootProject = project.rootProject
            if (!rootProject.plugins.hasPlugin(GradleLoggerPlugin::class.java)) {
                throw IllegalStateException(
                    "Failed to getLoggerService for project ${project.name}." +
                        "Apply com.avito.android.gradle-logger plugin to the root project"
                )
            }
            val extension = rootProject.extensions.getByType(GradleLoggerExtension::class.java)
            return registerLoggerServiceIfAbsent(project, extension)
        }

        /**
         * Inlined to skip anonymous class creation for map lambda
         * Breaks configuration cache with:
         *  > Could not load the value of field `transformer` of
         *  `org.gradle.api.internal.provider.TransformBackedProvider`
         *  > bean found in field `provider` of `org.gradle.api.internal.provider.MappingProvider`
         *  bean found in field `__loggerFactory__`
         */
        @Suppress("NOTHING_TO_INLINE")
        private inline fun getLoggerFactoryBuilder(
            project: Project,
            coordinates: GradleLoggerCoordinates
        ): Provider<GradleLoggerFactoryBuilder> {
            return getLoggerService(project)
                .map { it.createLoggerFactoryBuilder(coordinates) }
        }

        private fun checkProjectIsRoot(target: Project) {
            require(target.rootProject == target) {
                error
            }
        }

        private fun registerLoggerServiceIfAbsent(
            target: Project,
            extension: GradleLoggerExtension
        ): Provider<LoggerService> {
            return target.gradle.sharedServices.registerIfAbsent(
                LoggerService::javaClass.name,
                LoggerService::class.java
            ) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                    .withZone(ZoneId.from(ZoneOffset.UTC))
                val instant = Instant.now()
                it.parameters {
                    it.printlnHandler.set(extension.printlnHandler)
                    it.fileHandler.set(extension.fileHandler)
                    it.fileHandlerRootDir.set(extension.fileHandlerRootDir.map { it.dir(formatter.format(instant)) })
                    it.elasticHandler.set(extension.elasticHandler)
                    it.finalized.set(extension.finalized)
                }
            }
        }
    }
}
