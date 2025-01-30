package com.avito.teamcity.config

import com.avito.teamcity.model.TeamcityMetricsSource
import kotlinx.serialization.Serializable

@Serializable
internal data class TeamcityMetricsSourceConfig(
    val sources: List<TeamcityMetricsSource>
)
