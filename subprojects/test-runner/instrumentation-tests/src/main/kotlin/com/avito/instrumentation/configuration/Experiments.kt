package com.avito.instrumentation.configuration

import java.io.Serializable

public data class Experiments(
    val saveTestArtifactsToOutputs: Boolean,
    val useLegacyExtensionsV1Beta: Boolean,
    val disableLogcat: Boolean,
) : Serializable
