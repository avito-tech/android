package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.impact.configuration.sets.isAndroidTest
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import java.io.File

class AndroidTestConfiguration(module: InternalModule) : SimpleConfiguration(module) {

    override val isModified: Boolean by lazy {
        dependencies.any { it.isModified }
            || module.testConfiguration.isModified
            || hasChangedFiles
    }

    override val fullBytecodeSets: Set<File> by lazy {
        bytecodeSets() +
            dependencies.flatMap { it.fullBytecodeSets } +
            module.testConfiguration.fullBytecodeSets
    }

    override val dependencies: Set<ImplementationConfiguration> by lazy {
        project.configurations
            .filter { it.isAndroidTest() }
            .flatMap { configuration ->
                configuration
                    .dependencies
                    .withType(DefaultProjectDependency::class.java)
            }
            .toSet()
            .map {
                it.dependencyProject
                    .internalModule
                    .implementationConfiguration
            }
            .toSet()
    }

    override fun containsSources(sourceSet: AndroidSourceSet) = sourceSet.isAndroidTest()
    override fun containsBytecode(bytecodeDirectory: File): Boolean = bytecodeDirectory.isAndroidTest()

    override fun toString(): String {
        return "AndroidTestConfiguration(${project.path})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AndroidTestConfiguration

        if (project != other.project) return false

        return true
    }

    override fun hashCode(): Int {
        return project.hashCode()
    }
}
