package com.avito.emcee

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import java.time.Duration

public abstract class EmceeExtension(objects: ObjectFactory) {

    @get:Nested
    internal abstract val job: JobConfiguration

    @get:Nested
    internal abstract val artifactory: ArtifactoryConfiguration

    public abstract val retries: Property<Int>

    public abstract val deviceApis: ListProperty<Int>

    public abstract val testTimeout: Property<Duration>

    public abstract val queueBaseUrl: Property<String>

    public val configTestMode: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    public abstract val outputDir: DirectoryProperty

    public fun job(action: Action<JobConfiguration>) {
        action.execute(job)
    }

    public fun artifactory(action: Action<ArtifactoryConfiguration>) {
        action.execute(artifactory)
    }
}
