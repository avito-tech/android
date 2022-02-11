package com.avito.android.sentry

import io.sentry.SentryClient
import io.sentry.SentryClientFactory
import io.sentry.connection.NoopConnection
import io.sentry.context.ThreadLocalContextManager
import java.io.Serializable

public fun sentryClient(config: SentryConfig): SentryClient {
    return when (config) {
        is SentryConfig.Disabled ->
            SentryClient(NoopConnection(), ThreadLocalContextManager())

        is SentryConfig.Enabled ->
            SentryClientFactory.sentryClient(
                config.dsn,
                CustomizableSentryClientFactory(config.maxStringLength)
            ).apply {
                environment = config.environment
                serverName = config.serverName
                release = config.release
                config.tags.forEach { (key, value) ->
                    addTag(key, value)
                }
            }
    }
}

/**
 * Some payloads are bigger than default 400 symbols but helpful for com.avito.android.test.report.troubleshooting
 *
 * https://github.com/getsentry/sentry-java/issues/543
 */
private const val DEFAULT_SENTRY_MAX_STRING: Int = 50_000

/**
 * Default config for SentryClient
 */
public sealed class SentryConfig : Serializable {

    public object Disabled : SentryConfig()

    public data class Enabled(
        val dsn: String,
        val environment: String,
        val serverName: String,
        val release: String?,
        val tags: Map<String, String>,
        val maxStringLength: Int = DEFAULT_SENTRY_MAX_STRING
    ) : SentryConfig()
}
