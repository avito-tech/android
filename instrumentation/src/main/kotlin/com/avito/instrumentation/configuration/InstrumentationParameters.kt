package com.avito.instrumentation.configuration

import com.avito.report.model.ReportCoordinates
import java.io.Serializable

data class InstrumentationParameters(
    val initialParameters: Map<String, String> = emptyMap()
) : Map<String, String> by initialParameters, Serializable {

    fun applyParameters(newParameters: Map<String, String>): InstrumentationParameters =
        InstrumentationParameters(
            HashMap(this).apply {
                putAll(newParameters)
            }
        )

    fun reportCoordinates(): ReportCoordinates = ReportCoordinates(
        planSlug = getValue("planSlug"),
        jobSlug = getValue("jobSlug"),
        runId = getValue("runId")
    )
}
