package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.Experiments
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension

internal class ExperimentsConfigurator(
    private val extension: InstrumentationTestsPluginExtension
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        task.experiments.set(getExperiments())
    }

    private fun getExperiments(): Experiments {
        return Experiments(
            saveTestArtifactsToOutputs = getSaveTestArtifactsToOutputs(extension),
            useLegacyExtensionsV1Beta = getUseLegacyExtensionsV1Beta(extension),
            disableLogcat = getLogcatDisabled(extension)
        )
    }

    private fun getSaveTestArtifactsToOutputs(extension: InstrumentationTestsPluginExtension): Boolean {
        return extension.experimental.saveTestArtifactsToOutputs.getOrElse(false)
    }

    private fun getUseLegacyExtensionsV1Beta(extension: InstrumentationTestsPluginExtension): Boolean {
        return extension.experimental.useLegacyExtensionsV1Beta.getOrElse(false)
    }

    private fun getLogcatDisabled(extension: InstrumentationTestsPluginExtension): Boolean {
        return extension.experimental.disableLogcat.getOrElse(false)
    }
}
