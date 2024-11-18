package com.avito

import com.avito.android.publish.CreateStagingRepositoryTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskProvider
import org.gradle.plugins.signing.Sign

class PublishSonatypePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply(PublishBasePlugin::class.java)
            plugins.apply("signing")

            val ossrhUsername: Provider<String> = providers.gradleProperty("avito.ossrh.user")
            val ossrhPassword: Provider<String> = providers.gradleProperty("avito.ossrh.password")
            val ossrhStagingProfileId: Provider<String> = providers.gradleProperty("avito.ossrh.stagingProfileId")

            val sonatypeRepoName = "SonatypeReleases"

            val repositoryUrlOutputFilePath: Provider<RegularFile> =
                rootProject.layout.buildDirectory.file("sonatype-repo.id")

            val buildId: Provider<String> = providers.gradleProperty("teamcityBuildId")

            val createStagingRepositoryTask: TaskProvider<CreateStagingRepositoryTask> = with(rootProject.tasks) {
                val createStagingTaskName = "createSonatypeStagingRepository"

                try {
                    @Suppress("UNCHECKED_CAST")
                    named(createStagingTaskName) as TaskProvider<CreateStagingRepositoryTask>
                } catch (e: UnknownTaskException) {
                    register(createStagingTaskName, CreateStagingRepositoryTask::class.java) {
                        it.apply {
                            group = "publication"
                            stagingProfileId.set(ossrhStagingProfileId)
                            user.set(ossrhUsername)
                            password.set(ossrhPassword)
                            repositoryDescription.set("Release v.$version; build ${buildId.get()}")
                            repositoryIdFile.set(repositoryUrlOutputFilePath)
                        }
                    }
                }
            }

            signing {
                it.sign(publishing.publications)

                val signingKeyId = providers.gradleProperty("avito.pgp.keyid").orNull
                val signingKey = providers.gradleProperty("avito.pgp.key").orNull
                val signingPassword = providers.gradleProperty("avito.pgp.password").orNull

                it.useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            }

            tasks.withType(PublishToMavenRepository::class.java).configureEach { publishTask ->
                publishTask.apply {
                    // https://docs.gradle.org/current/userguide/publishing_customization.html#sec:configuring_publishing_tasks
                    if (name.contains(sonatypeRepoName)) {
                        doFirst {

                            // no direct task access, because "cannot be cast to class CreateStagingRepositoryTask" for some reason
                            val repositoryUrl = repositoryUrlOutputFilePath.get().asFile.readText()
                            repository = repository.apply { setUrl(repositoryUrl) }
                        }
                        dependsOn(createStagingRepositoryTask)
                        dependsOn(tasks.withType(Sign::class.java))
                    }
                }
            }

            val publishTask = tasks.register("publishToSonatype") {
                it.group = "publication"
                it.dependsOn(tasks.named("publishAllPublicationsTo${sonatypeRepoName}Repository"))
            }

            publishing.apply {
                repositories.maven { repo ->
                    repo.name = sonatypeRepoName
                    repo.credentials { cred ->
                        cred.username = ossrhUsername.orNull
                        cred.password = ossrhPassword.orNull
                    }
                }
            }

            tasks.withType(Sign::class.java).configureEach { signTask ->
                signTask.onlyIf {
                    gradle.taskGraph.hasTask(publishTask.get())
                }
            }
        }
    }
}
