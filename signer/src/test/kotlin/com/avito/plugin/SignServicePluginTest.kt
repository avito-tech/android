package com.avito.plugin

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SignServicePluginTest {

    @TempDir
    lateinit var testProjectDir: File

    private val mockWebServer = MockWebServer()

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @BeforeEach
    fun setup() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    versionCode = "100",
                    versionName = "22.1",
                    plugins = listOf("com.avito.android.signer"),
                    buildGradleExtra = """
                         signService {
                            host = "${mockWebServer.url("/")}"
                            apk(android.buildTypes.release, project.properties.get("signToken"))
                            bundle(android.buildTypes.release, project.properties.get("signToken"))
                         }
                    """.trimIndent()
                )
            )
        ).generateIn(testProjectDir)
    }

    @Test
    fun `plugin apply fails - without required params - when sign tasks in graph (on ci)`() {
        val result = ciRun(testProjectDir, ":app:signApkViaServiceRelease", expectFailure = true)
        result.assertThat().buildFailed("can't sign")
    }

    @Test
    fun `plugin applied - without required params - with allowSkip`() {
        val result = gradlew(testProjectDir, ":app:signApkViaServiceRelease", "-Pavito.signer.allowSkip=true")
        result.assertThat().buildSuccessful()
    }

    /**
     * Проверка :android [com.avito.android.bundleFileProvider]. Здесь, т.к. signer - единственный потребительно этого метода
     * Путь к bundle не смогли достать из API AGP, строим из пути к APK и уже поймали баг на обновлении до AGP 3.5
     * (к пути добавился buildVariant, которого не было в 3.4)
     */
    @Test
    fun `bundle path check`() {

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val result = gradlew(
            testProjectDir,
            ":app:signBundleViaServiceRelease",
            "-PsignToken=12345"
        )
        result.assertThat().buildSuccessful()

        val request = mockWebServer.takeRequest()

        val body = request.body.readString(Charsets.UTF_8)

        assertThat(body).contains("filename=\"app-release.aab\"")
    }

    @Test
    fun `sign apk task - runs after package`() {
        val result = gradlew(testProjectDir, ":app:signApkViaServiceRelease", dryRun = true)

        result.assertThat().tasksShouldBeTriggered(
            ":app:packageRelease",
            ":app:signApkViaServiceRelease"
        ).inOrder()
    }
}
