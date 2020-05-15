package com.avito.ci

import com.avito.test.gradle.KotlinModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CiStepsDynamicTest {

    @Test
    fun `cd plugin - custom task created`(@TempDir projectDir: File) {
        projectDir.file(
            name = "build.gradle",
            content = """
            plugins {
                id("com.avito.android.cd")
            }
            
            builds {
                myCustomTask {
                    description.set("My customTask description")
                }
            }
        """.trimIndent()
        )

        gradlew(projectDir, "tasks", "-Pci=true").assertThat()
            .buildSuccessful()
            .outputContains("myCustomTask - My customTask description")
    }

    @Test
    fun `cd plugin - custom task created without description`(@TempDir projectDir: File) {
        projectDir.file(
            name = "build.gradle",
            content = """
            plugins {
                id("com.avito.android.cd")
            }
            
            builds {
                myCustomTask {
                }
            }
        """.trimIndent()
        )

        gradlew(projectDir, "tasks", "-Pci=true").assertThat()
            .buildSuccessful()
            .outputContains("myCustomTask")
    }

    @Test
    fun `cd plugin - custom task with same name created in multiple projects`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.cd"),
            buildGradleExtra = """
            builds {
                myCustomTask {
                    description.set("My customTask description from root project")
                }
            }    
            """.trimIndent(),
            modules = listOf(
                KotlinModule(
                    name = "someModule",
                    plugins = listOf("com.avito.android.cd"),
                    buildGradleExtra = """
                    builds {
                        myCustomTask {
                            description.set("My customTask description from inner project")
                        }
                    }       
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "myCustomTask", "-Pci=true").assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":myCustomTask", ":someModule:myCustomTask")
    }
}
