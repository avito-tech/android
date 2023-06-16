package com.avito.android.tech_budget.internal.module_types

import com.avito.android.OwnerSerializer
import com.avito.android.module_type.ModuleTypeExtension
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.di.ApiServiceProvider
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.module_types.models.ModuleWithType
import com.avito.android.tech_budget.internal.module_types.models.UploadModuleTypesRequest
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

internal abstract class UploadModuleTypesTask : DefaultTask() {
    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializer>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun upload() {
        val dumpConfiguration = dumpInfoConfiguration.get()

        val api = ApiServiceProvider(
            baseUrl = dumpConfiguration.baseUploadUrl.get(),
            ownerAdapter = DefaultOwnerAdapter { ownerSerializer.get() },
            loggerFactory = loggerFactory.get()
        ).provide<UploadModuleTypesApi>()

        api.dumpModuleTypes(
            UploadModuleTypesRequest(
                dumpInfo = DumpInfo.fromExtension(dumpConfiguration),
                modules = collectModulesWithTypes()
            )
        ).executeWithHttpFailure(errorMessage = "Upload lint issues request failed")
    }

    private fun collectModulesWithTypes(): List<ModuleWithType> {
        val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()
        val getModuleFunctionalTypeName = techBudgetExtension.getModuleFunctionalTypeName.get()

        return project.subprojects.mapNotNull { subproject ->
            val type = subproject.extensions.findByType<ModuleTypeExtension>()?.type?.get()
                ?: return@mapNotNull null

            ModuleWithType(
                moduleName = subproject.path,
                functionalType = getModuleFunctionalTypeName(type)
            )
        }
    }

    companion object {
        const val NAME = "uploadModuleTypes"
    }
}
