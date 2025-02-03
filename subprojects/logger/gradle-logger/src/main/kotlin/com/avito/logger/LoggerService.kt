@file:Suppress("UnstableApiUsage")

package com.avito.logger

import com.avito.android.elastic.ElasticClientFactory
import com.avito.logger.builder.GradleLoggerFactoryBuilder
import com.avito.logger.destination.ElasticLoggingHandlerProvider
import com.avito.logger.handler.FileLoggingHandlerProvider
import com.avito.logger.handler.PrintlnLoggingHandlerProvider
import com.avito.logger.metadata.runtime.NoOpLoggerRuntimeMetadataProvider
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

public abstract class LoggerService : BuildService<LoggerService.Params> {

    public interface Params : BuildServiceParameters {
        public val fileHandler: Property<LogLevel>
        public val fileHandlerRootDir: DirectoryProperty
        public val printlnHandler: Property<GradleLoggerExtension.PrintlnMode>
        public val elasticHandler: Property<GradleLoggerExtension.Elastic>
        public val finalized: Property<Boolean>
    }

    init {
        require(parameters.finalized.getOrElse(false)) {
            "gradleLogger extension must be finalized. You trying to use it too early"
        }
    }

    private val printlnHandlerProvider: PrintlnLoggingHandlerProvider by lazy {
        with(parameters) {
            if (printlnHandler.isPresent) {
                val config = printlnHandler.get()
                PrintlnLoggingHandlerProvider(
                    config.level, config.printStackTrace, config.printMessageTime
                )
            } else {
                PrintlnLoggingHandlerProvider(
                    LogLevel.INFO,
                    printStackTrace = false,
                    printMessageTime = false
                )
            }
        }
    }

    private val fileHandlerProvider: FileLoggingHandlerProvider? by lazy {
        with(parameters) {
            if (fileHandler.isPresent) {
                FileLoggingHandlerProvider(
                    fileHandler.get(),
                    fileHandlerRootDir.get().asFile
                )
            } else {
                null
            }
        }
    }

    private val elasticHandlerProvider: ElasticLoggingHandlerProvider? by lazy {
        with(parameters) {
            if (elasticHandler.isPresent) {
                val config = elasticHandler.get()
                ElasticLoggingHandlerProvider(
                    config.level,
                    ElasticClientFactory.provide(config.config),
                    NoOpLoggerRuntimeMetadataProvider
                )
            } else {
                null
            }
        }
    }

    public fun createLoggerFactory(coordinates: GradleLoggerCoordinates): LoggerFactory {
        return createLoggerFactoryBuilder(coordinates).build()
    }

    public fun createLoggerFactoryBuilder(coordinates: GradleLoggerCoordinates): GradleLoggerFactoryBuilder =
        GradleLoggerFactoryBuilder(
            metadataProvider = GradleMetadataProvider(coordinates),
            println = printlnHandlerProvider,
            file = fileHandlerProvider,
            elastic = elasticHandlerProvider
        )
}
