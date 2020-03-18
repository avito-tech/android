package com.avito.ci

import com.avito.teamcity.TeamcityApi
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.delimiter
import kotlinx.cli.required

object ClearK8SDeploymentsMain {
    private val parser = ArgParser("clear-k8s-deployments")

    private val teamcityUrl: String by parser
        .option(type = ArgType.String)
        .required()

    private val teamcityApiUser: String by parser
        .option(type = ArgType.String)
        .required()

    private val teamcityApiPassword: String by parser
        .option(type = ArgType.String)
        .required()

    private val kubernetesToken by parser
        .option(ArgType.String)
        .required()

    private val kubernetesUrl by parser
        .option(ArgType.String)
        .required()

    private val kubernetesCaCert by parser
        .option(ArgType.String)
        .required()

    private val namespaces by parser
        .option(
            type = ArgType.String,
            description = "Kubernetes namespaces where need clear"
        )
        .delimiter(",")
        .required()

    @JvmStatic
    fun main(args: Array<String>) {
        parser.parse(args)
        ClearK8SDeployments(
            teamcity = TeamcityApi.Impl(
                url = teamcityUrl,
                user = teamcityApiUser,
                password = teamcityApiPassword
            ),
            kubernetesClient = DefaultKubernetesClient(
                ConfigBuilder()
                    .withOauthToken(kubernetesToken)
                    .withMasterUrl(kubernetesUrl)
                    .withCaCertData(kubernetesCaCert)
                    .build()
            )
        ).clear(namespaces)
    }
}