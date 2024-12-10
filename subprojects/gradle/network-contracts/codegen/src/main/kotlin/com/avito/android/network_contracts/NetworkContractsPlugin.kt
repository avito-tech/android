package com.avito.android.network_contracts

import com.avito.android.network_contracts.codegen.CodegenTask
import com.avito.android.network_contracts.codegen.SetupTmpMtlsFilesTask
import com.avito.android.network_contracts.configuration.codegenConfiguration
import com.avito.android.network_contracts.extension.NetworkContractsModuleExtension
import com.avito.android.network_contracts.internal.http.HttpClientService
import com.avito.android.network_contracts.scheme.fixation.collect.CollectApiSchemesTask
import com.avito.android.network_contracts.scheme.fixation.upsert.UpdateRemoteApiSchemesTask
import com.avito.android.network_contracts.scheme.imports.ApiSchemesImportTask
import com.avito.android.network_contracts.shared.networkContractsExtension
import com.avito.android.network_contracts.shared.networkContractsRootExtension
import com.avito.android.network_contracts.shared.reportFile
import com.avito.android.network_contracts.validation.ValidateNetworkContractsRootTask
import com.avito.android.network_contracts.validation.ValidateNetworkContractsSchemesTask
import com.avito.capitalize
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.toOptional
import com.avito.kotlin.dsl.typedNamed
import com.avito.kotlin.dsl.withType
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

public class NetworkContractsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(!target.isRoot()) {
            "NetworkContractsPlugin should not be applied to root"
        }

        createNetworkContractsExtension(target)

        target.codegenConfiguration.setArtifactsExecutable()

        target.plugins.withType<KotlinBasePlugin> {
            registerCodegenVariantsTask(target)
        }

        configureAddEndpointTask(target)
        configureValidationTask(target)
        configureCollectSchemesTask(target)
    }

    private fun createNetworkContractsExtension(project: Project) {
        project.extensions.create<NetworkContractsModuleExtension>(NetworkContractsModuleExtension.NAME)
    }

    private fun registerCodegenVariantsTask(target: Project) {
        // KMP type is not supported for now, using only single target (android/jvm)
        val kotlinTargetExtension = target.kotlinExtension as? KotlinSingleTargetExtension<*> ?: return

        kotlinTargetExtension.target.compilations
            .all { compilation ->
                // configure codegen task only for Android/Kotlin modules and include only baseVariant/main sourceSet
                val codegenTask = when {
                    compilation.isAndroidBaseVariantCompilation() -> registerCodegenTask(
                        name = CodegenTask.NAME,
                        variant = compilation.androidVariant.name,
                        target = target
                    )

                    compilation.isJvmMainCompilation() -> registerCodegenTask(
                        name = CodegenTask.NAME,
                        target = target
                    )

                    else -> return@all
                }

                compilation.defaultSourceSet.kotlin.srcDirs(codegenTask.flatMap { it.outputDirectory })
            }
    }

    private fun registerCodegenTask(
        name: String,
        target: Project,
        variant: String = "",
        forceValidation: Boolean = false,
        action: (CodegenTask) -> Unit = {}
    ): TaskProvider<CodegenTask> {
        val networkContractsExtension = target.networkContractsExtension
        val rootExtension = target.networkContractsRootExtension
        val setupMtlsTask = target.rootProject.tasks.named(
            SetupTmpMtlsFilesTask.NAME,
            SetupTmpMtlsFilesTask::class.java
        )

        val taskName = "$name${variant.capitalize()}"
        val outputDirectory = if (variant.isEmpty()) {
            networkContractsExtension.generatedDirectory.dir("main")
        } else {
            networkContractsExtension.generatedDirectory.dir(variant)
        }

        return target.tasks.register(taskName, CodegenTask::class.java) {
            it.packageName.set(networkContractsExtension.packageName)
            it.apiClassName.set(networkContractsExtension.apiClassName)
            it.moduleName.set(it.project.path)
            it.kind.set(networkContractsExtension.kind)
            it.codegenProjectName.set(networkContractsExtension.projectName)
            it.skipValidation.set(networkContractsExtension.skipValidation.map { !forceValidation && it })
            it.moduleDirectory.set(it.project.layout.projectDirectory)
            it.outputDirectory.set(outputDirectory)

            val codegenConfiguration = target.codegenConfiguration.takeIf { !it.isEmpty }
                ?: target.rootProject.codegenConfiguration
            it.codegenExecutableFiles.setFrom(codegenConfiguration.files)

            it.schemesDir.set(networkContractsExtension.apiSchemesDirectory)

            it.crtEnvName.set(rootExtension.crtEnvName)
            it.keyEnvName.set(rootExtension.keyEnvName)

            if (forceValidation) {
                it.tmpCrtFile.set(setupMtlsTask.flatMap { it.tmpCrt })
                it.tmpKeyFile.set(setupMtlsTask.flatMap { it.tmpKey })
            }

            it.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(it))

            it.onlyIf { (it as? CodegenTask)?.schemesDir?.get()?.asFileTree?.isEmpty == false }
            action.invoke(it)
        }
    }

    private fun configureAddEndpointTask(
        project: Project,
    ) {
        val httpClientService = HttpClientService.provideHttpClientService(project)
        val networkContractsModuleExtension = project.networkContractsExtension
        project.tasks.register(ApiSchemesImportTask.NAME, ApiSchemesImportTask::class.java) {
            it.apiPath.set(project.getOptionalStringProperty("apiSchemesUrl", ""))
            it.outputDirectory.set(
                networkContractsModuleExtension
                    .apiSchemesDirectory
                    .flatMap { it.dir(networkContractsModuleExtension.schemesDirName) }
            )

            it.httpClientBuilder.set(httpClientService)
            it.usesService(httpClientService)

            it.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(project))
        }
    }

    private fun configureValidationTask(
        project: Project
    ) {
        val rootTask = project.rootProject.tasks
            .withType<ValidateNetworkContractsRootTask>()

        val codegenTasks = registerCodegenTask(
            name = CodegenTask.NAME,
            variant = "validate",
            target = project,
            forceValidation = true
        )

        val validateSchemesTask = project.tasks
            .register<ValidateNetworkContractsSchemesTask>(ValidateNetworkContractsSchemesTask.NAME) {
                this.projectPath.set(project.path)
                this.schemes.from(codegenTasks.map { it.schemesDir })
                this.codegenTomlFilePath.set(
                    project.objects.fileProperty()
                        .convention(project.layout.projectDirectory.file(project.provider { "codegen.toml" }))
                        .toOptional()
                )
                this.failFast.set(project.networkContractsExtension.failFast)
                this.verdictFile.set(
                    project.reportFile(
                        directory = "networkContracts",
                        reportFileName = "codegenValidationSchemesVerdict.txt"
                    )
                )

                resultFile.set(
                    project.reportFile(
                        directory = "networkContracts",
                        reportFileName = "codegenValidationSchemesReport.json"
                    )
                )
            }

        rootTask.configureEach {
            it.reports.from(validateSchemesTask.map { it.resultFile })
        }
    }

    private fun configureCollectSchemesTask(project: Project) {
        val extension = project.networkContractsExtension

        val updateApiSchemesTask = project.rootProject.tasks
            .typedNamed<UpdateRemoteApiSchemesTask>(UpdateRemoteApiSchemesTask.NAME)

        val collectApiSchemesTask = project.tasks.register<CollectApiSchemesTask>(CollectApiSchemesTask.NAME) {
            projectPath.set(project.path)
            projectName.set(extension.projectName)
            codegenTomlFile.set(
                project.layout.projectDirectory
                    .asFileTree
                    .matching { it.include("**/codegen.toml") }
                    .firstOrNull()
            )
            schemesDirectory.set(extension.apiSchemesDirectory.flatMap { it.dir(extension.schemesDirName) })

            jsonSchemeMetadataFile.set(
                project.reportFile(
                    directory = "networkContracts",
                    reportFileName = "${CollectApiSchemesTask.NAME}.json"
                )
            )
        }

        updateApiSchemesTask.configure {
            it.schemes.from(collectApiSchemesTask.flatMap(CollectApiSchemesTask::jsonSchemeMetadataFile))
        }
    }
}

@OptIn(ExperimentalContracts::class)
private fun KotlinCompilation<*>.isAndroidBaseVariantCompilation(): Boolean {
    contract {
        returns(true) implies (this@isAndroidBaseVariantCompilation is KotlinJvmAndroidCompilation)
    }
    return this is KotlinJvmAndroidCompilation &&
        this.androidVariant.baseName == this.androidVariant.name
}

@OptIn(ExperimentalContracts::class)
private fun KotlinCompilation<*>.isJvmMainCompilation(): Boolean {
    contract {
        returns(true) implies (this@isJvmMainCompilation is KotlinWithJavaCompilation<*, *>)
    }
    return this is KotlinWithJavaCompilation<*, *> && this.name == "main"
}
