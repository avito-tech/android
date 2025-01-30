package com.avito.teamcity.model

import kotlinx.serialization.Serializable

@Serializable
internal data class TeamcityMetricsSource(
    val configurationId: String,
    val fetchIntervalInHours: Long,
)
