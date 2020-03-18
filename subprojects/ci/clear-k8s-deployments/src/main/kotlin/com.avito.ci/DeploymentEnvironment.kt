package com.avito.ci

import io.fabric8.kubernetes.api.model.apps.Deployment

sealed class DeploymentEnvironment {
    data class Teamcity(val buildId: String) : DeploymentEnvironment()
    data class Local(val creationTimeInMillis: Long) : DeploymentEnvironment()
    object Unknown : DeploymentEnvironment()
}

val Deployment.environment: DeploymentEnvironment
    get() {
        val type = metadata.labels.get("type")
        return when {
            type != null && type.startsWith("teamcity") -> {
                DeploymentEnvironment.Teamcity(
                    buildId = requireNotNull(metadata.labels["id"])
                )
            }
            type != null && type.startsWith("local") -> {
                DeploymentEnvironment.Local(
                    creationTimeInMillis = requireNotNull(metadata.labels["id"]).toLong()
                )
            }
            // todo remove after 2020.3.4 release
            type == null && metadata.name.split('-')
                .run { size > 0 && get(0).toLongOrNull() != null } -> {
                DeploymentEnvironment.Teamcity(
                    buildId = metadata.name.split('-')[0]
                )
            }
            else -> DeploymentEnvironment.Unknown
        }
    }