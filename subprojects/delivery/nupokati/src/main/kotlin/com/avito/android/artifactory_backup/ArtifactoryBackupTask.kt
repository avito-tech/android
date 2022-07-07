package com.avito.android.artifactory_backup

import com.avito.android.http.ArtifactoryClient
import com.avito.android.http.createArtifactoryHttpClient
import com.avito.android.stats.StatsDConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class ArtifactoryBackupTask : DefaultTask() {

    @get:Input
    public abstract val artifactoryUser: Property<String>

    @get:Input
    public abstract val artifactoryPassword: Property<String>

    /**
     * full url to artifactory directory
     * example: http://<artifactory-host>/artifactory/mobile-releases/avito_android/118.0_2/
     */
    @get:Input
    public abstract val artifactoryUploadPath: Property<String>

    @get:Input
    public abstract val buildVariant: Property<String>

    @get:InputFiles
    public abstract val files: Property<FileCollection>

    @get:Internal
    public abstract val statsDConfig: Property<StatsDConfig>

    @get:OutputFile
    public abstract val buildOutput: RegularFileProperty

    @TaskAction
    public fun doWork() {
        val httpClient = createArtifactoryHttpClient(
            user = artifactoryUser.get(),
            password = artifactoryPassword.get(),
            statsDConfig = statsDConfig.get()
        )

        val artifactoryClient = ArtifactoryClient(httpClient)
        val artifactsAdapter = CdBuildResultArtifactsAdapter()

        val artifactoryBackupAction = ArtifactoryBackupAction(
            artifactoryClient = artifactoryClient,
            artifactsAdapter = artifactsAdapter,
            logger = logger
        )

        val artifactsJson = artifactoryBackupAction.backup(
            artifactoryUploadPath = artifactoryUploadPath.get(),
            buildVariant = buildVariant.get(),
            files = files.get().files
        )

        this.buildOutput.get().asFile.writeText(artifactsJson)
    }
}
