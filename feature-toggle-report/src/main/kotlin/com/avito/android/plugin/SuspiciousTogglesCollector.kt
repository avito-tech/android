package com.avito.android.plugin

import org.gradle.api.logging.Logger
import java.time.LocalDate

typealias Team = String
typealias DeveloperEmail = String

internal class SuspiciousTogglesCollector(
    private val logger: Logger,
    private val developerToTeam: Map<DeveloperEmail, Team>
) {

    fun collectSuspiciousToggles(
        jsonTogglesList: List<JsonToggle>,
        blameCodeLinesList: List<CodeElement>,
        turnedOnDateAgo: LocalDate,
        turnedOffDateAgo: LocalDate
    ): List<Toggle> {
        return jsonTogglesList
            .filter { it.value is Boolean }
            .asSequence()
            .map { jsonToggle ->
                val codeElement = blameCodeLinesList.find { it.codeLine.contains(jsonToggle.key) }
                val toggleIndex = blameCodeLinesList.indexOf(codeElement)
                val lastChangeCodeItem = blameCodeLinesList
                    .subList(toggleIndex, blameCodeLinesList.size)
                    .find { it.codeLine.contains("defaultValue") }

                if (lastChangeCodeItem == null) {
                    logger.error("Error: ${jsonToggle.key}")
                    null
                } else {
                    Toggle(
                        toggleName = jsonToggle.key,
                        isOn = jsonToggle.value.toString().toBoolean(),
                        changeDate = lastChangeCodeItem.changeTime,
                        team = resolveTeam(lastChangeCodeItem)
                    )
                }
            }
            .filterNotNull()
            .filter {
                (it.isOn && it.changeDate < turnedOnDateAgo)
                    || (!it.isOn && it.changeDate < turnedOffDateAgo)
            }
            .filterNot { excludedToggles.contains(it.toggleName) }
            .toList()
    }

    private fun resolveTeam(lastChangeCodeItem: CodeElement): Team =
        developerToTeam[lastChangeCodeItem.email] ?: "undefined"

    private val excludedToggles = listOf(
        "stetho",
        "certificate_pinning",
        "leak_canary",
        "schema_check"
    )
}
