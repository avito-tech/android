package com.avito.teamcity.config

import com.avito.teamcity.model.TeamcityMetricsSource
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class TeamcityMetricsSourceConfigDeserializationTest {

    @Test
    fun deserialization() {
        val configJson = """
            |{
            |   "sources": [
            |       {"configurationId": "configuration1", "fetchIntervalInHours": "3"},
            |       {"configurationId": "configuration2", "fetchIntervalInHours": "10"}
            |   ]
            |}
        """.trimMargin()
        val config = Json.decodeFromString<TeamcityMetricsSourceConfig>(configJson)
        assertThat(config).isEqualTo(TeamcityMetricsSourceConfig(
            sources = listOf(
                TeamcityMetricsSource("configuration1", 3),
                TeamcityMetricsSource("configuration2", 10),
            )
        ))
    }
}
