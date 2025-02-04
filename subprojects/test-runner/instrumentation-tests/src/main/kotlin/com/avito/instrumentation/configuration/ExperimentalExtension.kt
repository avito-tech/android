package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property

public abstract class ExperimentalExtension {

    /**
     * By default artifacts uploaded to file storage, and if file storage disabled you can't access it
     * This is not intended behavior and should be changed
     */
    public abstract val saveTestArtifactsToOutputs: Property<Boolean>

    public abstract val useLegacyExtensionsV1Beta: Property<Boolean>

    public abstract val disableLogcat: Property<Boolean>
}
