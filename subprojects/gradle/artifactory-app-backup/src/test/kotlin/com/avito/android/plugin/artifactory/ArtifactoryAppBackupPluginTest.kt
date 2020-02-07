package com.avito.android.plugin.artifactory

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.http.MockDispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private typealias Artifact = Pair<String, String>

internal class ArtifactoryAppBackupPluginTest {

    private val mockWebServer = MockWebServer()

    @Test
    fun `artifactory plugin - captures app parameters`(@TempDir projectDir: File) {
        val moduleName = "app"
        val backupName = "backupName"
        val backupType = "backupType"
        val backupVersion = "backupVersion"
        val classifier = "releaseApk"
        val artifactName = "xxx.apk"
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    versionCode = "90",
                    versionName = "10",
                    name = moduleName,
                    plugins = listOf("com.avito.android.artifactory-app-backup"),
                    buildGradleExtra = """
                    """.trimIndent(),
                    customScript = """
                        import static com.avito.android.plugin.artifactory.ArtifactoryAppBackupInterfaceKt.getArtifactoryAndroidArtifactsBuildVariants
                        import com.avito.cd.BuildVariant
                        
                        $artifactoryBackupExtensionName {
                            backup {
                                name = "$backupName"
                                type = "$backupType"
                                version = "$backupVersion"
                                artifact {
                                    id = "$classifier"
                                    path = "$artifactName"
                                }
                            }
                        }
                        
                        getArtifactoryAndroidArtifactsBuildVariants(project).put("$classifier", BuildVariant.STAGING)
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        Files.createFile(Paths.get(projectDir.path, moduleName, artifactName))

        val dispatcher = MockDispatcher(defaultResponse = MockResponse().setResponseCode(200))
            .also { mockWebServer.setDispatcher(it) }

        dispatcher.mockResponse(
            requestMatcher = { path.contains("maven-metadata.xml") },
            response = MockResponse().setResponseCode(200).setFakeMavenMetadataBody()
        )

        val rootPomRequest = dispatcher.captureRequest { path.endsWith(".pom") }
        val putApkRequest =
            dispatcher.captureRequest { path.endsWith(".apk") && method.toLowerCase() == "put" }

        val result = ciRun(
            projectDir,
            ":$moduleName:${artifactoryAppBackupTaskName}",
            "-PartifactoryUrl=${mockWebServer.url("/")}",
            "-Partifactory_deployer=xxx",
            "-Partifactory_deployer_password=xxx"
        )

        result
            .assertThat()
            .buildSuccessful()

        rootPomRequest.checks.singleRequestCaptured().apply {
            pathContains("$backupName/$backupType/$backupVersion/$backupType-$backupVersion")
        }

        putApkRequest.checks.singleRequestCaptured().apply {
            pathContains("$backupName/$backupType/$backupVersion/$backupType-$backupVersion-$classifier.apk")
        }
    }

    @Test
    fun `upload to artifactory - success - multiple files with same extension`(@TempDir projectDir: File) {
        val moduleName = "app"
        val backupName = "backupName"
        val backupType = "backupType"
        val backupVersion = "backupVersion"
        val artifacts = setOf<Artifact>(
            "id1" to "xxx1.json",
            "id2" to "xxx2.json"
        )
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    versionCode = "90",
                    versionName = "10",
                    name = moduleName,
                    plugins = listOf("com.avito.android.artifactory-app-backup"),
                    buildGradleExtra = """
                    """.trimIndent(),
                    customScript = """
                        $artifactoryBackupExtensionName {
                            backup {
                                name = "$backupName"
                                type = "$backupType"
                                version = "$backupVersion"
                                ${artifacts.map { (id, path) ->
                        """
                                artifact {
                                    id = "$id"
                                    path = "$path"
                                }    
                                """.trimIndent()
                    }.joinToString(separator = "\n")}
                            }
                        }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        artifacts.forEach { (_, path) ->
            Files.createFile(Paths.get(projectDir.path, moduleName, path))
        }

        val dispatcher = MockDispatcher(defaultResponse = MockResponse().setResponseCode(200))
            .also { mockWebServer.setDispatcher(it) }

        dispatcher.mockResponse(
            requestMatcher = { path.contains("maven-metadata.xml") },
            response = MockResponse().setResponseCode(200).setFakeMavenMetadataBody()
        )

        val rootPomRequest = dispatcher.captureRequest { path.endsWith(".pom") }
        val putJsonFileRequests =
            artifacts.map { (id, _) -> dispatcher.captureRequest { path.endsWith("$id.json") && method.toLowerCase() == "put" } }

        val result = ciRun(
            projectDir,
            ":$moduleName:${artifactoryAppBackupTaskName}",
            "-PartifactoryUrl=${mockWebServer.url("/")}",
            "-Partifactory_deployer=xxx",
            "-Partifactory_deployer_password=xxx"
        )

        result
            .assertThat()
            .buildSuccessful()

        rootPomRequest.checks.singleRequestCaptured().apply {
            pathContains("$backupName/$backupType/$backupVersion/$backupType-$backupVersion")
        }

        artifacts.forEachIndexed { index, (id, _) ->
            putJsonFileRequests[index].checks.singleRequestCaptured()
                .pathContains("$backupName/$backupType/$backupVersion/$backupType-$backupVersion-$id.json")
        }
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }
}
