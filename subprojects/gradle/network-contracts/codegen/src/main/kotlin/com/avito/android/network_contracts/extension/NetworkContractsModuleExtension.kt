package com.avito.android.network_contracts.extension

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class NetworkContractsModuleExtension(
    objects: ObjectFactory,
) {

    public abstract val kind: Property<String>

    public abstract val projectName: Property<String>

    public abstract val packageName: Property<String>

    public abstract val apiClassName: Property<String>

    public abstract val version: Property<String>

    public val failFast: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    public val skipValidation: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    public val schemesDirName: Property<String> = objects.property<String>()
        .convention("api-clients")

    public val apiSchemesDirectory: DirectoryProperty = objects.directoryProperty()

    public val generatedDirectory: DirectoryProperty = objects.directoryProperty()

    internal companion object {
        internal const val NAME = "networkContracts"
    }
}
