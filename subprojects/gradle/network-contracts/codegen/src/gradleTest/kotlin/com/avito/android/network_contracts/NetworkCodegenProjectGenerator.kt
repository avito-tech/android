package com.avito.android.network_contracts

import com.avito.android.network_contracts.scheme.imports.ApiSchemesFilesGenerator
import com.avito.android.network_contracts.scheme.imports.data.models.SchemaEntry
import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.module.Module
import com.avito.test.gradle.plugin.plugins
import org.intellij.lang.annotations.Language
import java.io.File

internal const val DEFAULT_APP_NAME = "avito-android-test-app"
internal const val DEFAULT_GENERATED_PACKAGE = "com.example.test"
internal const val DEFAULT_API_SCHEMES_DIRECTORY = "src/main/resources/"
internal const val DEFAULT_BUILD_DIRECTORY = "build/networkContracts/codegen"

internal fun defaultAndroidModule(
    name: String = "impl",
    appName: String = DEFAULT_APP_NAME,
    generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
    apiSchemesDirectory: String = DEFAULT_API_SCHEMES_DIRECTORY,
    generatedDirectory: String = DEFAULT_BUILD_DIRECTORY,
    skipValidation: Boolean = true,
    buildExtra: String = "",
) = AndroidLibModule(
    name = name,
    plugins = plugins {
        id("com.avito.android.network-contracts")
    },
    buildGradleExtra = buildGradleExtra(
        appName,
        generatedClassesPackage,
        skipValidation,
        apiSchemesDirectory,
        generatedDirectory,
        buildExtra,
    ),
    useKts = true
)

internal fun defaultModule(
    name: String = "impl",
    appName: String = DEFAULT_APP_NAME,
    generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
    apiSchemesDirectory: String = DEFAULT_API_SCHEMES_DIRECTORY,
    generatedDirectory: String = DEFAULT_BUILD_DIRECTORY,
    skipValidation: Boolean = true,
    failFast: Boolean = false,
    buildExtra: String = "",
): KotlinModule {
    return KotlinModule(
        name = name,
        plugins = plugins {
            id("com.avito.android.network-contracts")
        },
        buildGradleExtra = buildGradleExtra(
            appName,
            generatedClassesPackage,
            skipValidation,
            apiSchemesDirectory,
            generatedDirectory,
            buildExtra,
            failFast,
        ),
        useKts = true
    )
}

private fun buildGradleExtra(
    appName: String = DEFAULT_APP_NAME,
    generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
    skipValidation: Boolean = true,
    apiSchemesDirectory: String = DEFAULT_API_SCHEMES_DIRECTORY,
    generatedDirectory: String = DEFAULT_BUILD_DIRECTORY,
    buildExtra: String = "",
    failFast: Boolean = false,
): String {
    return """
        networkContracts {
            kind.set("test-kind")
            projectName.set("$appName")
            packageName.set("$generatedClassesPackage")
            skipValidation.set($skipValidation)
            failFast.set($failFast)
            apiSchemesDirectory.set(project.layout.projectDirectory.dir("$apiSchemesDirectory"))
            generatedDirectory.set(project.layout.projectDirectory.dir("$generatedDirectory"))
        }
        $buildExtra
    """.trimIndent()
}

object NetworkCodegenProjectGenerator {

    fun generate(
        projectDir: File,
        serviceUrl: String = "www.avito.ru/",
        generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
        skipValidation: Boolean = true,
        modules: List<Module> = listOf(
            defaultModule(
                generatedClassesPackage = generatedClassesPackage,
                skipValidation = skipValidation,
            )
        ),
        @Language("kotlin") buildExtra: String = ""
    ) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.network-contracts-root")
                id("com.avito.android.tls-configuration")
            },
            imports = listOf(),
            buildGradleExtra = """
                ${createMtlsExtensionString()}   
                networkContractsRoot {
                    useTls.set(false)
                    serviceUrl.set("$serviceUrl")
                    crtEnvName.set("test_env")
                    keyEnvName.set("test_key")
                }
                $buildExtra
            """.trimIndent(),
            modules = modules,
            useKts = true
        ).generateIn(projectDir)
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun generateSchemes(
        projectDir: File,
        moduleName: String? = null,
        apiSchemesDirectory: String = DEFAULT_API_SCHEMES_DIRECTORY,
        schemes: List<SchemaEntry> = emptyList(),
    ): List<File> {
        if (schemes.isEmpty()) {
            return emptyList()
        }

        val apiSchemesDir = File(
            projectDir,
            "$apiSchemesDirectory/api-clients"
        )
        val generatedFiles = ApiSchemesFilesGenerator(apiSchemesDir).generateFiles(schemes)
        val codegenFile = File(projectDir, "codegen.toml")
        codegenFile.createNewFile()
        return generatedFiles + codegenFile
    }

    internal fun generateCodegenFiles(
        moduleName: String,
        projectDir: File,
        generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
        generatedFiles: List<File> = emptyList(),
    ): List<File> {
        if (generatedFiles.isEmpty()) {
            return emptyList()
        }

        val generatedDir = File(
            projectDir,
            "$moduleName/src/main/kotlin/" +
                generatedClassesPackage.replace(".", "/") +
                "/generated"
        )

        return generatedFiles.map {
            File(generatedDir, it.path).apply {
                parentFile.mkdirs()
                if (!exists()) {
                    createNewFile()
                }
            }
        }
    }
}
