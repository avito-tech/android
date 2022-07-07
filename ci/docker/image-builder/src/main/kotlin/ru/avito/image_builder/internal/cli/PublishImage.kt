package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.ImagePublisher
import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.command.RegistryLoginImpl
import ru.avito.image_builder.internal.command.SimpleImageBuilder
import ru.avito.image_builder.internal.docker.CliDocker
import ru.avito.image_builder.internal.docker.RegistryCredentials
import java.io.File

@OptIn(ExperimentalCli::class)
internal class PublishImage(
    name: String,
    description: String
) : BuildImage(name, description) {

    private val registryUsername: String by option(
        type = ArgType.String,
        description = "Docker registry username"
    ).required()

    private val registryPassword: String by option(
        type = ArgType.String,
        description = "Docker registry password"
    ).required()

    override fun execute() {
        val docker = CliDocker()

        val builder = SimpleImageBuilder(
            docker = docker,
            dockerfilePath = dockerfilePath,
            buildDir = File(buildDir),
            login = dockerHubLogin(docker),
            tagger = ImageTagger(docker),
            registry = registry,
            artifactoryUrl = artifactoryUrl,
            imageName = imageName,
        )

        val publisher = ImagePublisher(
            docker = docker,
            builder = builder,
            login = RegistryLoginImpl(
                docker = docker,
                credentials = RegistryCredentials(
                    registry = registry,
                    username = registryUsername,
                    password = registryPassword,
                )
            )
        )
        publisher.publish()
    }
}
