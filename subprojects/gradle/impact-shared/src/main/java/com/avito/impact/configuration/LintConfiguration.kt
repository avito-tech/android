@file:Suppress("DEPRECATION")
// todo use new api?

package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.module.configurations.ConfigurationType.Lint

class LintConfiguration(module: InternalModule) : BaseConfiguration(module, setOf(Lint::class.java)) {

    override val isModified: Boolean by lazy {
        dependencies.any { it.isModified }
            || module.androidTestConfiguration.isModified
            || hasChangedFiles
    }

    override fun containsSources(sourceSet: AndroidSourceSet) = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LintConfiguration
        if (project != other.project) return false
        return true
    }

    override fun hashCode(): Int {
        return project.hashCode()
    }
}
