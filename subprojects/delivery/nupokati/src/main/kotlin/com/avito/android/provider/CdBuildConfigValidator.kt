package com.avito.android.provider

import com.avito.android.model.CdBuildConfig

internal interface CdBuildConfigValidator {

    fun validate(config: CdBuildConfig)
}

internal class StrictCdBuildConfigValidator : CdBuildConfigValidator {

    override fun validate(config: CdBuildConfig) {
        checkUnsupportedDeployments(config)
        checkQappsDeployments(config)
    }

    private fun checkUnsupportedDeployments(config: CdBuildConfig) {
        val unknownDeployments = config.deployments.filterIsInstance<CdBuildConfig.Deployment.Unknown>()
        require(unknownDeployments.isEmpty()) {
            "Unknown deployment types: $unknownDeployments"
        }
    }

    private fun checkQappsDeployments(config: CdBuildConfig) {
        val deployments = config.deployments.filterIsInstance<CdBuildConfig.Deployment.Qapps>()
        require(deployments.size <= 1) {
            "Must be one Qapps deployment, but was: $deployments"
        }
        if (deployments.isNotEmpty()) {
            require(config.schemaVersion >= 2) {
                "Qapps deployments is supported only in the 2'nd version of contract"
            }
        }
    }
}
