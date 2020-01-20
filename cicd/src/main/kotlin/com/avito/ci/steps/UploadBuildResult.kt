package com.avito.ci.steps

import com.avito.android.plugin.artifactory.artifactoryAppBackupTask
import com.avito.android.plugin.artifactory.artifactoryPassword
import com.avito.android.plugin.artifactory.artifactoryUser
import com.avito.cd.UploadCdBuildResultTask
import com.avito.cd.cdBuildConfig
import com.avito.cd.isCdBuildConfigPresent
import com.avito.cd.uploadCdBuildResultTaskName
import com.avito.kotlin.dsl.namedOrNull
import com.avito.upload_to_googleplay.deployTaskName
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

class UploadBuildResult(context: String) : SuppressibleBuildStep(context) {

    var uiTestConfiguration: String? = null

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (project.isCdBuildConfigPresent) {
            val uiTestConfiguration = uiTestConfiguration
            require(!uiTestConfiguration.isNullOrBlank()) {
                "You must set uiTestConfiguration"
            }
            val uploadCdBuildResult = project.tasks.register<UploadCdBuildResultTask>(
                name = uploadCdBuildResultTaskName,
                constructorArgs = *arrayOf(
                    uiTestConfiguration,
                    project.artifactoryUser,
                    project.artifactoryPassword,
                    project.cdBuildConfig.get().outputDescriptor
                )
            ) {
                group = "cd"
                description = "Task for send CD build result"
                project.tasks.namedOrNull(deployTaskName)?.also { deployTask -> dependsOn(deployTask) }
                mustRunAfter(project.tasks.artifactoryAppBackupTask())
            }
            rootTask.configure { it.finalizedBy(uploadCdBuildResult) }
        }
    }
}
