package com.avito.instrumentation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.build_verdict.span.SpannedString.Companion.link
import com.avito.android.build_verdict.span.SpannedString.Companion.multiline
import com.avito.android.getApk
import com.avito.android.getApkOrThrow
import com.avito.android.stats.StatsDConfig
import com.avito.gradle.worker.inMemoryWork
import com.avito.instrumentation.configuration.Experiments
import com.avito.instrumentation.internal.RunnerInputDumper
import com.avito.logger.GradleLoggerPlugin
import com.avito.runner.config.InstrumentationConfigurationCacheableData
import com.avito.runner.config.InstrumentationConfigurationDataFactory
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.config.RunnerReportConfig
import com.avito.runner.finalizer.verdict.InstrumentationTestsTaskVerdict
import com.avito.runner.model.InstrumentationParameters
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.runner.scheduler.TestSchedulerFactoryProvider
import com.avito.runner.scheduler.runner.scheduler.TestSchedulerResult
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisMode
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisResult
import com.avito.test.model.DeviceName
import com.avito.utils.BuildFailer
import com.avito.utils.gradle.KubernetesCredentials
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.time.Duration
import javax.inject.Inject

@CacheableTask
public abstract class InstrumentationTestsTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask(), BuildVerdictTask {

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val application: DirectoryProperty

    @get:Optional
    @get:Input
    public abstract val applicationPackageName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val testApplication: DirectoryProperty

    @get:Input
    public abstract val testApplicationPackageName: Property<String>

    @get:Optional
    @get:Input
    public abstract val testArtifactsDirectoryPackageName: Property<String>

    @get:Input
    public abstract val impactAnalysisMode: Property<ImpactAnalysisMode>

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val changedTests: RegularFileProperty

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val applicationProguardMapping: RegularFileProperty

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val testProguardMapping: RegularFileProperty

    @get:Internal
    public abstract val buildId: Property<String>

    @get:Internal
    public abstract val buildType: Property<String>

    @get:Internal
    public abstract val gitCommit: Property<String>

    @get:Internal
    public abstract val gitBranch: Property<String>

    @get:Input
    public abstract val experiments: Property<Experiments>

    @get:Input
    public abstract val suppressFailure: Property<Boolean>

    @get:Input
    public abstract val suppressFlaky: Property<Boolean>

    @get:Input
    public abstract val instrumentationConfiguration: Property<InstrumentationConfigurationCacheableData>

    @get:Internal
    public abstract val mergedInstrumentationParams: MapProperty<String, String>

    @get:Internal
    public abstract val reportConfig: Property<RunnerReportConfig>

    @get:Internal
    public abstract val targetInstrumentationParams: MapProperty<DeviceName, InstrumentationParameters>

    @get:Input
    public abstract val instrumentationRunner: Property<String>

    @get:Input
    public abstract val logcatTags: SetProperty<String>

    @get:Input
    public abstract val enableDeviceDebug: Property<Boolean>

    @get:Input
    public abstract val gradleTestKitRun: Property<Boolean>

    @get:Input
    public abstract val projectName: Property<String>

    @get:Internal
    public abstract val kubernetesCredentials: Property<KubernetesCredentials>

    @get:Internal
    public abstract val kubernetesHttpTries: Property<Int>

    @get:Internal
    public abstract val buildFailer: Property<BuildFailer>

    @get:Internal
    public abstract val statsDConfig: Property<StatsDConfig>

    @get:Internal
    public abstract val adbPullTimeout: Property<Duration>

    @get:OutputDirectory
    public abstract val output: DirectoryProperty

    @get:OutputDirectory
    @get:Optional
    public abstract val macrobenchmarkOutputDirectory: DirectoryProperty

    private val verdictFile = objects.fileProperty().convention(output.file("verdict.json"))

    @get:Internal
    override val verdict: SpannedString
        get() {
            val verdictFile = verdictFile.asFile.get()
            return if (verdictFile.exists()) {
                val gson: Gson = GsonBuilder().setPrettyPrinting().create()
                val verdictRaw = verdictFile.reader()
                val verdict = gson.fromJson(verdictRaw, InstrumentationTestsTaskVerdict::class.java)
                return multiline(
                    lines = listOf(link(verdict.reportUrl, verdict.title)) +
                        verdict.problemTests.map { test ->
                            link(test.testUrl, test.title)
                        }
                )
            } else {
                SpannedString("Can't find verdict file $verdictFile")
            }
        }

    @TaskAction
    public fun doWork() {
        val configuration = instrumentationConfiguration.get()
        val experiments = experiments.get()
        val output = output.get().asFile

        val testRunParams = RunnerInputParams(
            mainApk = requireNotNull(application.orNull?.getApk()) {
                "Unable to create valid RunnerInputParams - mainApk cannot be null."
            },
            testApk = testApplication.get().getApkOrThrow(),
            instrumentationConfiguration = InstrumentationConfigurationDataFactory(
                instrumentationConfigurationCacheableData = configuration,
                mergedInstrumentationParams = InstrumentationParameters(mergedInstrumentationParams.get()),
                reportConfig = reportConfig.get(),
                targetInstrumentationParams = targetInstrumentationParams.get(),
            ).create(),
            executionParameters = ExecutionParameters(
                applicationPackageName.get(),
                testApplicationPackageName.get(),
                testArtifactsDirectoryPackageName.getOrElse(applicationPackageName.get()),
                instrumentationRunner.get(),
                logcatTags.get(),
            ),
            buildId = buildId.get(),
            buildType = buildType.get(),
            kubernetesCredentials = requireNotNull(kubernetesCredentials.orNull) {
                "you need to provide kubernetesCredentials"
            },
            kubernetesHttpTries = kubernetesHttpTries.get(),
            deviceDebug = enableDeviceDebug.get(),
            projectName = projectName.get(),
            suppressFailure = suppressFailure.get(),
            suppressFlaky = suppressFlaky.get(),
            impactAnalysisResult = ImpactAnalysisResult.create(
                mode = impactAnalysisMode.get(),
                changedTestsFile = changedTests.asFile.orNull
            ),
            outputDir = output,
            verdictFile = verdictFile.get().asFile,
            statsDConfig = statsDConfig.get(),
            proguardMappings = listOf(
                applicationProguardMapping,
                testProguardMapping
            ).mapNotNull { it.orNull?.asFile },
            saveTestArtifactsToOutputs = experiments.saveTestArtifactsToOutputs,
            useLegacyExtensionsV1Beta = experiments.useLegacyExtensionsV1Beta,
            disableLogcat = experiments.disableLogcat,
            adbPullTimeout = adbPullTimeout.get(),
            macrobenchmarkOutputDir = macrobenchmarkOutputDirectory.orNull?.asFile,
        )

        val isGradleTestKitRun = gradleTestKitRun.get()

        RunnerInputDumper(output).dumpInput(
            input = testRunParams,
            isGradleTestKitRun = isGradleTestKitRun
        )

        if (!isGradleTestKitRun) {
            val loggerFactory = GradleLoggerPlugin.provideLoggerFactory(this).get()
            workerExecutor.inMemoryWork {
                when (
                    val result = TestSchedulerFactoryProvider(loggerFactory)
                        .provide(testRunParams)
                        .create()
                        .schedule()
                ) {
                    TestSchedulerResult.Ok -> {
                        // do nothing
                    }

                    is TestSchedulerResult.Failure -> buildFailer.get().failBuild(result.message)
                }
            }
        }
    }
}
