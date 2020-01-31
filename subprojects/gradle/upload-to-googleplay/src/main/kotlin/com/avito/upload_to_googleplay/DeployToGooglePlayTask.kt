package com.avito.upload_to_googleplay

import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

const val deployTaskName = "deployToGooglePlay"

fun TaskContainer.registerDeployToGooglePlayTask(
    deploys: List<GooglePlayDeploy>,
    configuration: Task.() -> Unit
): TaskProvider<out DefaultTask> {
    return register(deployTaskName, DeployToGooglePlayTask::class.java, deploys).apply {
        configure {
            it.description = "Upload binary to google play"
            it.group = "Google play"
        }
        configure(configuration)
    }
}

internal abstract class DeployToGooglePlayTask @Inject constructor(
    private val deploys: List<GooglePlayDeploy>
) : DefaultTask() {

    private val jsonKey = project.playConsoleJsonKey
    private val logger = this.ciLogger

    @TaskAction
    fun upload() {
        val googlePlayKey = (jsonKey.orNull
            ?: throw IllegalStateException("google play key must present in ${project.name}").apply {
                logger.critical("google play key was empty", this)
            })
        val deployer = GooglePlayDeployer.Impl(googlePlayKey, logger)
        deployer.deploy(deploys)
    }
}
