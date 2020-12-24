package com.avito.logger

import java.io.Serializable

data class LoggerMetadata(
    val tag: String,
    val pluginName: String? = null,
    val projectPath: String? = null,
    val taskName: String? = null
) : Serializable
