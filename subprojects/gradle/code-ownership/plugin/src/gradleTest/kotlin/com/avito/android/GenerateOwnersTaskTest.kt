package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class GenerateOwnersTaskTest {

    @Test
    internal fun `generate Owners file - file created and filled with data`(@TempDir projectDir: File) {
        projectDir.dir("common/code-owners/src/main/kotlin/com/avito/android/ownership").file("Owners.kt")
        projectDir.dir(".bitbucket").file("CodeOwnersExclusions.csv").also {
            it.writeText(CODE_OWNERS_EXCLUSIONS_FILE_TEXT)
        }
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            useKts = true,
            imports = listOf(
                "import com.avito.android.network.FakeAvitoOwnersClient",
                "import com.avito.android.network.FakeAlertinoSender",
            ),
            buildGradleExtra = """
                ownership {
                    avitoOwnersClient.set(FakeAvitoOwnersClient())
                    alertinoSender.set(FakeAlertinoSender())
                    ownersDir.set(project(":common:code-owners").layout.projectDirectory)
                }
                    """.trimMargin(),
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                    },
                    imports = listOf(
                        "import com.avito.android.model.AvitoCodeOwner",
                        "import com.avito.android.model.Unit",
                        "import com.avito.android.network.FakeAvitoOwnersClient",
                        "import com.avito.android.network.FakeAlertinoSender",
                    ),
                    dependencies = setOf(
                        project(
                            path = ":common",
                            configuration = IMPLEMENTATION
                        )
                    ),
                    buildGradleExtra = """
                        object Speed : AvitoCodeOwner {
                            override val type = Unit("Speed", "1")
                            override fun toString() = "Speed"
                        }
                        object MobileArchitecture : AvitoCodeOwner {
                            override val type = Unit("Mobile Architecture", "2")
                            override fun toString() = "Mobile Architecture"
                        }
                       
                        ownership {
                            owners(Speed, MobileArchitecture)
                            avitoOwnersClient.set(FakeAvitoOwnersClient())
                            alertinoSender.set(FakeAlertinoSender())
                            ownersDir.set(project(":common:code-owners").layout.projectDirectory)
                        }
                    """.trimMargin(),
                    useKts = true,
                ),
                AndroidLibModule(
                    name = "common",
                    modules = listOf(
                        AndroidLibModule(
                            name = "code-owners",
                        )
                    )
                ),
            )
        ).generateIn(projectDir)

        val file = File(projectDir, "common/code-owners/src/main/kotlin/com/avito/android/ownership/Owners.kt")

        runGenerateOwners(projectDir)
            .assertThat()
            .buildSuccessful()

        assertThat(file.exists()).isTrue()

        assertThat(file.readText()).isEqualTo(OWNERS_FILE_TEXT)
    }

    @Test
    internal fun `generate CODEOWNERS file - file created and filled with data`(@TempDir projectDir: File) {
        projectDir.dir("common/code-owners/src/main/kotlin/com/avito/android/ownership").file("Owners.kt")
        projectDir.dir(".bitbucket").file("CODEOWNERS")
        projectDir.dir(".bitbucket").file("CodeOwnersExclusions.csv").also {
            it.writeText(CODE_OWNERS_EXCLUSIONS_FILE_TEXT)
        }
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            useKts = true,
            imports = listOf(
                "import com.avito.android.network.FakeAvitoOwnersClient",
                "import com.avito.android.network.FakeAlertinoSender",
            ),
            buildGradleExtra = """
                ownership {
                    avitoOwnersClient.set(FakeAvitoOwnersClient())
                    alertinoSender.set(FakeAlertinoSender())
                    ownersDir.set(projectDir)
                }
                    """.trimMargin(),
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                    },
                    imports = listOf(
                        "import com.avito.android.model.AvitoCodeOwner",
                        "import com.avito.android.model.Unit",
                        "import com.avito.android.network.FakeAvitoOwnersClient",
                        "import com.avito.android.network.FakeAlertinoSender",
                    ),
                    buildGradleExtra = """
                        object Speed : AvitoCodeOwner {
                            override val type = Unit("Speed", "1")
                            override fun toString() = "Speed"
                        }
                        object MobileArchitecture : AvitoCodeOwner {
                            override val type = Unit("Mobile Architecture", "2")
                            override fun toString() = "Mobile Architecture"
                        }
                       
                        ownership {
                            owners(Speed, MobileArchitecture)
                            avitoOwnersClient.set(FakeAvitoOwnersClient())
                            alertinoSender.set(FakeAlertinoSender())
                            ownersDir.set(project(":common:code-owners").layout.projectDirectory)
                        }
                    """.trimMargin(),
                    useKts = true,
                ),
                AndroidAppModule(
                    name = "common",
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                    },
                    imports = listOf(
                        "import com.avito.android.model.AvitoCodeOwner",
                        "import com.avito.android.model.Unit",
                        "import com.avito.android.network.FakeAvitoOwnersClient",
                        "import com.avito.android.network.FakeAlertinoSender",
                    ),
                    buildGradleExtra = """
                        object Speed : AvitoCodeOwner {
                            override val type = Unit("Speed", "1")
                            override fun toString() = "Speed"
                        }
                       
                        ownership {
                            owners(Speed)
                            avitoOwnersClient.set(FakeAvitoOwnersClient())
                            alertinoSender.set(FakeAlertinoSender())
                            ownersDir.set(projectDir)
                        }
                    """.trimMargin(),
                    useKts = true,
                    modules = listOf(
                        AndroidAppModule(
                            name = "code-owners",
                        )
                    )
                ),
            )
        ).generateIn(projectDir)

        val file = File(projectDir, ".bitbucket/CODEOWNERS")

        runGenerateOwners(projectDir)
            .assertThat()
            .buildSuccessful()

        assertThat(file.exists()).isTrue()

        assertThat(file.readText()).isEqualTo(CODE_OWNERS_FILE_TEXT)
    }

    private fun runGenerateOwners(projectDir: File) = gradlew(
        projectDir,
        "generateCodeOwnersFile",
        useTestFixturesClasspath = true,
    )

    private companion object {

        private val CODE_OWNERS_EXCLUSIONS_FILE_TEXT = """
            module,ownerIds
        """.trimIndent()

        private val CODE_OWNERS_FILE_TEXT = """
            /app/ mobarch1@avito.ru mobarch2@avito.ru speed1@avito.ru speed2@avito.ru
            /common/ speed1@avito.ru speed2@avito.ru
        """.trimIndent()

        private val OWNERS_FILE_TEXT = """
            package com.avito.android.ownership

            import com.avito.android.model.AvitoCodeOwner
            import com.avito.android.model.Team
            import com.avito.android.model.Type
            import com.avito.android.model.Unit
            import kotlin.String
            import kotlin.collections.Set

            /**
             * !!! This file is autogenerated. Do not modify it by hands. !!!
             *
             * Use {@code ./gradlew generateCodeOwnersFile} command to update the owners.
             */
            public enum class Owners(
                override val type: Type,
                public val chatChannels: Set<String> = setOf(),
            ) : AvitoCodeOwner {
                Mobile_Architecture_Unit(
                    Unit(
                        name = "Mobile Architecture",
                        id = "2"
                    ),
                    chatChannels = setOf()
                ),
                Speed_Team(
                    Team(
                        name = "Speed",
                        id = "1",
                        unit = Mobile_Architecture_Unit,
                    ),
                    chatChannels = setOf()
                ),
                ;
            }

        """.trimIndent()
    }
}
