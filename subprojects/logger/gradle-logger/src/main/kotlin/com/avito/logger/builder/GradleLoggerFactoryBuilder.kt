package com.avito.logger.builder

import com.avito.android.elastic.ElasticClientFactory
import com.avito.logger.GradleLoggerExtension
import com.avito.logger.GradleMetadataProvider
import com.avito.logger.LogLevel
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggerFactoryBuilder
import com.avito.logger.destination.ElasticLoggingHandlerProvider
import com.avito.logger.handler.FileLoggingHandlerProvider
import com.avito.logger.handler.PrintlnLoggingHandlerProvider
import com.avito.logger.metadata.runtime.LoggerRuntimeMetadataProvider
import java.io.File

public class GradleLoggerFactoryBuilder internal constructor(
    private val metadataProvider: GradleMetadataProvider,
    private var println: PrintlnLoggingHandlerProvider,
    private var file: FileLoggingHandlerProvider? = null,
    private var elastic: ElasticLoggingHandlerProvider? = null,
) {
    public fun printlnHandler(
        config: GradleLoggerExtension.PrintlnMode,
    ): GradleLoggerFactoryBuilder {
        println = PrintlnLoggingHandlerProvider(
            acceptedLogLevel = config.level,
            printStackTrace = config.printStackTrace,
            printMessageTime = config.printMessageTime
        )
        return this
    }

    public fun fileHandler(
        logLevel: LogLevel,
        rootDir: File,
    ): GradleLoggerFactoryBuilder {
        file = FileLoggingHandlerProvider(logLevel, rootDir)
        return this
    }

    public fun elasticHandlerProvider(
        config: GradleLoggerExtension.Elastic,
        runtimeMetadataProvider: LoggerRuntimeMetadataProvider,
    ): GradleLoggerFactoryBuilder {
        elastic = ElasticLoggingHandlerProvider(
            acceptedLogLevel = config.level,
            elasticClient = ElasticClientFactory.provide(config.config),
            runtimeMetadataProvider = runtimeMetadataProvider,
        )
        return this
    }

    public fun build(): LoggerFactory {
        val e = elastic
        val f = file
        val builder = LoggerFactoryBuilder()
        with(builder) {
            metadataProvider(metadataProvider)
            addLoggingHandlerProviders(buildList {
                add(println)
                if (f != null) {
                    add(f)
                }
                if (e != null) {
                    add(e)
                }
            })
        }
        return builder.build()
    }
}
