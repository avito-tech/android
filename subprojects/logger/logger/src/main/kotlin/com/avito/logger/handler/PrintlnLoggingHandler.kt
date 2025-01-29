package com.avito.logger.handler

import com.avito.logger.LogLevel
import com.avito.logger.metadata.LoggerMetadata
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

public class PrintlnLoggingHandlerProvider(
    private val acceptedLogLevel: LogLevel,
    private val printStackTrace: Boolean,
    private val printMessageTime: Boolean = false,
) : LoggingHandlerProvider {

    public override fun provide(
        metadata: LoggerMetadata
    ): LoggingHandler {
        return PrintlnLoggingHandler(
            acceptedLogLevel,
            printStackTrace = printStackTrace,
            printMessageTime = printMessageTime,
            messagePrefix = metadata.asMessagePrefix,
        )
    }
}

internal class PrintlnLoggingHandler(
    acceptedLogLevel: LogLevel,
    private val printStackTrace: Boolean,
    private val printMessageTime: Boolean,
    private val messagePrefix: String,
) : LogLevelLoggingHandler(acceptedLogLevel) {

    @delegate:Transient
    private val timeFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS", Locale.ROOT)
    }

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        println(buildString {
            append(messagePrefix)
            append(" ")
            if (printMessageTime) {
                append(timeFormat.format(Date(System.currentTimeMillis())))
                append(": ")
            }
            append(message)
        })
        if (printStackTrace && error != null) {
            error.printStackTrace()
        }
    }
}
