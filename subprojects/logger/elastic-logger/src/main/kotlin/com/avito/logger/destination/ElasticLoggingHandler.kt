package com.avito.logger.destination

import com.avito.android.elastic.ElasticClient
import com.avito.logger.LogLevel
import com.avito.logger.handler.LogLevelLoggingHandler
import com.avito.logger.metadata.runtime.LoggerRuntimeMetadataProvider

public class ElasticLoggingHandler(
    acceptedLogLevel: LogLevel,
    private val client: ElasticClient,
    private val metadata: Map<String, String>,
    private val runtimeMetadataProvider: LoggerRuntimeMetadataProvider,
) : LogLevelLoggingHandler(acceptedLogLevel) {

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        client.sendMessage(
            level = level.name,
            message = message,
            metadata = metadata + runtimeMetadataProvider.provide().asMap(),
            throwable = error
        )
    }
}
