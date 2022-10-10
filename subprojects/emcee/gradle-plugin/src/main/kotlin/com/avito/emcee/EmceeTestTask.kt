package com.avito.emcee

import com.avito.emcee.client.EmceeTestClientConfig
import com.avito.emcee.client.di.EmceeTestActionFactory
import com.avito.emcee.client.internal.ArtifactorySettings
import com.avito.emcee.internal.EmceeConfigTestHelper
import com.avito.emcee.internal.getApkOrThrow
import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.queue.Job
import com.avito.emcee.queue.ScheduleStrategy
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.time.Duration
import kotlin.time.ExperimentalTime

public abstract class EmceeTestTask : DefaultTask() {

    @get:Nested
    public abstract val job: Property<JobConfiguration>

    @get:Nested
    public abstract val artifactory: Property<ArtifactoryConfiguration>

    @get:Input
    public abstract val retries: Property<Int>

    @get:Input
    public abstract val devices: ListProperty<Device>

    @get:Internal // todo do we want to invalidate test results on host change?
    public abstract val baseUrl: Property<String>

    @get:Internal // todo do we want to invalidate test results on timeout increase?
    public abstract val testTimeout: Property<Duration>

    @get:InputDirectory
    public abstract val apk: DirectoryProperty

    @get:Input
    public abstract val appPackage: Property<String>

    @get:InputDirectory
    public abstract val testApk: DirectoryProperty

    @get:Input
    public abstract val testAppPackage: Property<String>

    @get:Input
    public abstract val testRunnerClass: Property<String>

    @get:Input
    public abstract val configTestMode: Property<Boolean>

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.getLoggerFactory(this)

    @ExperimentalTime
    @TaskAction
    public fun action() {

        val artifactory = artifactory.get()

        val artifactorySettings = ArtifactorySettings(
            baseUrl = artifactory.baseUrl.get(),
            user = artifactory.user.get(),
            password = artifactory.password.get(),
            repository = artifactory.repository.get(),
            folder = artifactory.folder.get()
        )

        val emceeTestAction = EmceeTestActionFactory
            .create(
                emceeQueueBaseUrl = baseUrl.get(),
                artifactorySettings = artifactorySettings,
                loggerFactory = loggerFactory.get()
            ).create()

        val job = job.get()
        val testTimeoutInSec = testTimeout.get().seconds

        val devices = devices.get().map { device -> DeviceConfiguration(device.type, device.sdk) }
        require(devices.isNotEmpty()) {
            "Failed to execute ${this.name}. There are no configured devices in extension"
        }
        val config = EmceeTestClientConfig(
            job = Job(
                id = job.id.get(),
                groupId = job.groupId.get(),
                priority = job.priority.get(),
                groupPriority = job.groupPriority.get(),
            ),
            artifactory = artifactorySettings,
            scheduleStrategy = ScheduleStrategy(testsSplitter = ScheduleStrategy.TestsSplitter.Individual),
            testExecutionBehavior = TestExecutionBehavior(
                environment = emptyMap(),
                retries = retries.get()
            ),
            testMaximumDurationSec = testTimeoutInSec,
            devices = devices,
            apk = apk.get().getApkOrThrow(), // todo should be optional for libraries
            appPackage = appPackage.get(),
            testApk = testApk.get().getApkOrThrow(),
            testAppPackage = testAppPackage.get(),
            testRunnerClass = testRunnerClass.get(),
        )

        // writing config dump every time for debug purposes
        EmceeConfigTestHelper(outputDir.get().asFile).serialize(config)

        if (!configTestMode.get()) {
            emceeTestAction.execute(config)
        }
    }
}
