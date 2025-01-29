package com.avito.android.elastic

import com.avito.logger.LogLevel
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggerFactoryBuilder
import com.avito.logger.handler.PrintlnLoggingHandlerProvider
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

public object ElasticClientFactory {

    private val loggerFactory: LoggerFactory = LoggerFactoryBuilder()
        .addLoggingHandlerProvider(
            PrintlnLoggingHandlerProvider(
                LogLevel.WARNING,
                printStackTrace = false,
                printMessageTime = false
            )
        )
        .build()

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    private val cache = mutableMapOf<ElasticConfig, ElasticClient>()

    private const val defaultTimeoutSec = 10L

    public fun provide(
        config: ElasticConfig,
    ): ElasticClient = when (config) {

        is ElasticConfig.Disabled -> StubElasticClient()

        is ElasticConfig.Enabled -> cache.getOrPut(
            key = config,
            defaultValue = {
                ElasticClientProvider(
                    config = config,
                    timeProvider = timeProvider,
                    loggerFactory = loggerFactory,
                    // TODO move to module
                    okHttpClient = OkHttpClient.Builder()
                        .connectTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
                        .readTimeout(defaultTimeoutSec, TimeUnit.SECONDS),
                    gsonBuilder = GsonBuilder(),
                ).provide()
            }
        )
    }
}
