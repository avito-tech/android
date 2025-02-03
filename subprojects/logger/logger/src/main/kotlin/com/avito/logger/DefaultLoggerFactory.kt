package com.avito.logger

import com.avito.logger.handler.CompositeLoggingHandler
import com.avito.logger.handler.LoggingHandlerProvider
import com.avito.logger.metadata.LoggerMetadataProvider
import com.avito.logger.metadata.TagLoggerMetadataProvider

internal class DefaultLoggerFactory(
    private val metadataProvider: LoggerMetadataProvider,
    private val handlerProviders: List<LoggingHandlerProvider>
) : LoggerFactory {

    init {
        check(handlerProviders.isNotEmpty()) {
            "handler providers must contain at least one provider"
        }
    }

    override fun create(tag: String): Logger {
        val metadata = metadataProvider.provide(tag)
        val handlers = handlerProviders.map { it.provide(metadata) }
        val handler = CompositeLoggingHandler(handlers)
        return LoggerImpl(handler)
    }
}

public class LoggerFactoryBuilder {

    private var metadataProvider: LoggerMetadataProvider = TagLoggerMetadataProvider
    private val handlerProviders = mutableListOf<LoggingHandlerProvider>()

    public fun metadataProvider(provider: LoggerMetadataProvider): LoggerFactoryBuilder {
        this.metadataProvider = provider
        return this
    }

    public fun addLoggingHandlerProvider(
        provider: LoggingHandlerProvider
    ): LoggerFactoryBuilder {
        handlerProviders.add(provider)
        return this
    }

    public fun addLoggingHandlerProviders(
        providers: List<LoggingHandlerProvider>
    ): LoggerFactoryBuilder {
        handlerProviders.addAll(providers)
        return this
    }

    public fun newBuilder(): LoggerFactoryBuilder {
        return LoggerFactoryBuilder().also { newBuilder ->
            newBuilder.metadataProvider = metadataProvider
            newBuilder.handlerProviders.addAll(handlerProviders)
        }
    }

    public fun build(): LoggerFactory {
        return DefaultLoggerFactory(
            metadataProvider = metadataProvider,
            handlerProviders = handlerProviders.toList()
        )
    }
}
